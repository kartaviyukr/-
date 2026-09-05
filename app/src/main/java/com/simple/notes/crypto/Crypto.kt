package com.simple.notes.crypto

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Вся криптография приложения.
 *
 * Ключевая идея: из ЛЮБОГО пароля детерминированно получается пара
 * (ключ шифрования, идентификатор хранилища). Поэтому «неверного» пароля
 * не существует — каждый пароль открывает своё собственное хранилище.
 * Приложение физически не может отличить основной пароль от любого другого.
 */
object Crypto {

    private const val PBKDF2_ITERATIONS = 120_000
    private const val DERIVED_BITS = 512 // 32 байта на ключ + 32 байта на идентификатор
    private const val IV_LENGTH = 12
    private const val TAG_BITS = 128

    private val random = SecureRandom()

    class Derived(val key: ByteArray, val vaultId: String) {
        fun wipe() = key.fill(0)
    }

    fun newSalt(): ByteArray = ByteArray(32).also { random.nextBytes(it) }

    fun randomBytes(size: Int): ByteArray = ByteArray(size).also { random.nextBytes(it) }

    /** Медленное преобразование пароля в ключ (PBKDF2, 120 000 итераций). Вызывать вне главного потока. */
    fun derive(password: CharArray, salt: ByteArray): Derived {
        val spec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, DERIVED_BITS)
        val material = try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
        val key = material.copyOfRange(0, 32)
        val vaultId = material.copyOfRange(32, 64).toHex()
        material.fill(0)
        return Derived(key, vaultId)
    }

    fun encrypt(key: ByteArray, plain: ByteArray): ByteArray {
        val iv = randomBytes(IV_LENGTH)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, iv))
        val body = cipher.doFinal(plain)
        return iv + body
    }

    /** Возвращает null, если данные повреждены или ключ не подходит. */
    fun decrypt(key: ByteArray, data: ByteArray): ByteArray? {
        if (data.size <= IV_LENGTH) return null
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(key, "AES"),
                GCMParameterSpec(TAG_BITS, data, 0, IV_LENGTH)
            )
            cipher.doFinal(data, IV_LENGTH, data.size - IV_LENGTH)
        } catch (e: Exception) {
            null
        }
    }

    private fun ByteArray.toHex(): String {
        val sb = StringBuilder(size * 2)
        for (b in this) sb.append("%02x".format(b))
        return sb.toString()
    }
}
