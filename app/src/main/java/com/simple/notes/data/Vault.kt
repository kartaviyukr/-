package com.simple.notes.data

import android.content.Context
import android.util.Base64
import com.simple.notes.crypto.Crypto
import org.json.JSONArray
import java.io.File
import java.util.UUID

/**
 * Открытое хранилище: набор заметок и фотографий, зашифрованных на ключе,
 * полученном из конкретного пароля.
 */
class Vault(val id: String, private val key: ByteArray, val dir: File) {

    private val notesFile = File(dir, "n.dat")
    private val mediaDir = File(dir, "m")

    /** После блокировки ключ обнулён, поэтому любые операции запрещены: иначе можно записать мусор. */
    @Volatile
    private var closed = false

    fun loadNotes(): List<Note> {
        if (closed || !notesFile.exists()) return emptyList()
        val plain = Crypto.decrypt(key, notesFile.readBytes()) ?: return emptyList()
        return try {
            val arr = JSONArray(String(plain, Charsets.UTF_8))
            (0 until arr.length()).map { Note.fromJson(arr.getJSONObject(it)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveNotes(notes: List<Note>) {
        if (closed) return
        val arr = JSONArray()
        notes.forEach { arr.put(it.toJson()) }
        writeAtomic(notesFile, Crypto.encrypt(key, arr.toString().toByteArray(Charsets.UTF_8)))
    }

    fun savePhoto(bytes: ByteArray): String? {
        if (closed) return null
        mediaDir.mkdirs()
        val photoId = UUID.randomUUID().toString().replace("-", "")
        writeAtomic(File(mediaDir, photoId), Crypto.encrypt(key, bytes))
        return photoId
    }

    fun readPhoto(photoId: String): ByteArray? {
        if (closed) return null
        val f = File(mediaDir, photoId)
        if (!f.exists()) return null
        return Crypto.decrypt(key, f.readBytes())
    }

    fun deletePhotos(photoIds: Collection<String>) {
        photoIds.forEach { File(mediaDir, it).delete() }
    }

    /** Имена всех файлов фотографий блокнота — нужны при переносе на новый пароль. */
    fun photoIds(): List<String> =
        if (closed) emptyList() else mediaDir.listFiles()?.map { it.name }.orEmpty()

    /** Сохраняет фотографию под уже существующим именем: ссылки в заметках не ломаются. */
    fun writePhoto(photoId: String, bytes: ByteArray) {
        if (closed) return
        mediaDir.mkdirs()
        writeAtomic(File(mediaDir, photoId), Crypto.encrypt(key, bytes))
    }

    /** Стирает ключ из памяти при блокировке приложения. */
    fun close() {
        closed = true
        key.fill(0)
    }

    private fun writeAtomic(target: File, data: ByteArray) {
        target.parentFile?.mkdirs()
        val tmp = File(target.parentFile, target.name + ".tmp")
        tmp.writeBytes(data)
        if (!tmp.renameTo(target)) {
            target.writeBytes(data)
            tmp.delete()
        }
    }
}

/**
 * Открытый блокнот и то, какой пароль его открыл.
 *
 * [enteredVaultId] — папка, которую даёт введённый пароль. Для настоящего
 * блокнота она совпадает с [Vault.id], а для чужого блокнота отличается:
 * пароль не подошёл ни к одной папке. По этой разнице приложение понимает,
 * что показывает подделку, — но наружу этого не показывает.
 */
class OpenedVault(val vault: Vault, val enteredVaultId: String) {
    val isDecoy: Boolean get() = vault.id != enteredVaultId
}

/**
 * Управляет всеми хранилищами на устройстве.
 *
 * Хранилище — это папка, имя которой выводится из пароля. Узнать, какая
 * из папок «настоящая», без пароля невозможно: все они выглядят одинаково
 * и зашифрованы одним и тем же алгоритмом.
 */
class VaultManager(context: Context) {

    private val prefs = context.getSharedPreferences("cfg", Context.MODE_PRIVATE)
    private val root = File(context.filesDir, "v")

    /** Задан ли уже основной пароль (то есть был ли первый запуск). */
    val isConfigured: Boolean
        get() = prefs.getBoolean("ready", false)

    /**
     * Первый запуск: каждый из паролей пользователя создаёт свой пустой блокнот.
     * Возвращает блокнот первого пароля, чтобы сразу его открыть.
     */
    fun createMainVaults(passwords: List<CharArray>): OpenedVault {
        val vaults = passwords.map { password ->
            val derived = Crypto.derive(password, salt())
            Vault(derived.vaultId, derived.key, File(root, derived.vaultId)).apply {
                dir.mkdirs()
                saveNotes(emptyList())
            }
        }
        openDecoyVault().close()
        createPadding()
        prefs.edit().putBoolean("ready", true).apply()
        vaults.drop(1).forEach { it.close() }
        return OpenedVault(vaults.first(), vaults.first().id)
    }

    /**
     * Добавляет ещё один блокнот со своим паролем.
     * Если папка такого пароля уже есть, не трогает её: чужие заметки затирать нельзя.
     */
    fun createProfile(password: CharArray) {
        val derived = Crypto.derive(password, salt())
        val dir = File(root, derived.vaultId)
        if (dir.exists()) {
            derived.wipe()
            return
        }
        dir.mkdirs()
        Vault(derived.vaultId, derived.key, dir).apply {
            saveNotes(emptyList())
            close()
        }
    }

    /** Совпадает ли пароль с папкой, которую открыли: проверка старого пароля при смене. */
    fun matchesVault(password: CharArray, vaultId: String): Boolean {
        val derived = Crypto.derive(password, salt())
        val same = derived.vaultId == vaultId
        derived.wipe()
        return same
    }

    /**
     * Переносит блокнот на новый пароль: заметки и фотографии перешифровываются
     * ключом нового пароля в новую папку, и только после этого старая удаляется.
     * Возвращает false, если папка нового пароля уже занята другим блокнотом.
     */
    fun changePassword(vault: Vault, newPassword: CharArray): Boolean {
        val derived = Crypto.derive(newPassword, salt())
        val target = File(root, derived.vaultId)
        if (target.exists()) {
            derived.wipe()
            return false
        }
        target.mkdirs()
        val moved = Vault(derived.vaultId, derived.key, target)
        moved.saveNotes(vault.loadNotes())
        vault.photoIds().forEach { photoId ->
            vault.readPhoto(photoId)?.let { moved.writePhoto(photoId, it) }
        }
        moved.close()
        vault.dir.deleteRecursively()
        return true
    }

    /**
     * Открывает хранилище, соответствующее введённому паролю.
     *
     * Папки настоящих блокнотов созданы при настройке паролей, поэтому такая
     * папка существует только для одного из паролей пользователя. Любой другой
     * пароль даёт имя папки, которой нет, — и открывается общий чужой блокнот.
     * Он всегда один и тот же: меняющиеся заметки выдали бы подделку.
     */
    fun openVault(password: CharArray): OpenedVault {
        val derived = Crypto.derive(password, salt())
        val dir = File(root, derived.vaultId)
        if (dir.exists()) return OpenedVault(Vault(derived.vaultId, derived.key, dir), derived.vaultId)
        val enteredVaultId = derived.vaultId
        derived.wipe()
        return OpenedVault(openDecoyVault(), enteredVaultId)
    }

    /**
     * Общий блокнот для всех неверных паролей. Его папка выводится из строки,
     * которую нельзя набрать на клавиатуре, поэтому она никогда не совпадёт
     * с папкой настоящего хранилища и выглядит так же, как остальные.
     */
    private fun openDecoyVault(): Vault {
        val derived = Crypto.derive(DECOY_KEY_SOURCE, salt())
        val dir = File(root, derived.vaultId)
        val vault = Vault(derived.vaultId, derived.key, dir)
        if (!dir.exists()) {
            dir.mkdirs()
            vault.saveNotes(Decoy.generate(derived.vaultId))
        }
        return vault
    }

    private fun salt(): ByteArray {
        prefs.getString("s", null)?.let { return Base64.decode(it, Base64.NO_WRAP) }
        val fresh = Crypto.newSalt()
        prefs.edit().putString("s", Base64.encodeToString(fresh, Base64.NO_WRAP)).apply()
        return fresh
    }

    /**
     * Создаёт несколько хранилищ на случайных ключах, которые не открываются
     * никаким паролем. Так по количеству папок нельзя понять, сколькими
     * паролями реально пользуются.
     */
    private fun createPadding() {
        val count = 2 + (System.nanoTime() % 4).toInt()
        repeat(count) {
            val fakeId = Crypto.randomBytes(32).joinToString("") { b -> "%02x".format(b) }
            val fakeKey = Crypto.randomBytes(32)
            val vault = Vault(fakeId, fakeKey, File(root, fakeId))
            vault.dir.mkdirs()
            vault.saveNotes(Decoy.generate(fakeId))
            vault.close()
        }
    }

    private companion object {
        /** Содержит управляющие символы: ввести такой «пароль» с клавиатуры нельзя. */
        val DECOY_KEY_SOURCE = charArrayOf('\u0000', 'd', 'e', 'c', 'o', 'y', '\u0000')
    }
}
