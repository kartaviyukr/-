package com.simple.notes.data

import java.util.Random
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Наполнение «чужих» хранилищ. Набор заметок детерминированно зависит от
 * идентификатора хранилища: один и тот же неверный пароль всегда показывает
 * один и тот же блокнот, а разные пароли — разные. Пустой экран после ввода
 * пароля выглядел бы подозрительно, поэтому его не бывает.
 */
object Decoy {

    private val POOL: List<Pair<String, String>> = listOf(
        "Продукты" to "молоко\nхлеб\nяйца\nсыр\nстиральный порошок\nбатарейки AA",
        "Купить к выходным" to "уголь для мангала\nминералка\nлёд\nсалфетки",
        "Аптека" to "витамин D\nпластыри\nкапли для носа",
        "Wi-Fi у Лены" to "сеть: HOME-2G\nпароль: 89124417",
        "Размеры" to "окно на кухне 142 x 87\nдверь в кладовку 60 x 195\nстол 120 x 70",
        "Машина" to "ТО пройти до конца месяца\nдавление в шинах 2.2\nмасло 5W-30\nстраховка до 14-го",
        "Фильмы посмотреть" to "Прибытие\nБольшой Лебовски\nВолна\nОхота",
        "Книги" to "Стругацкие, Град обречённый\nЧитать по 20 страниц вечером",
        "Рецепт: сырники" to "творог 400 г\nяйцо 1 шт\nсахар 2 ст. л.\nмука 3 ст. л.\nжарить на среднем огне",
        "Рецепт: плов" to "рис девзира 500 г\nморковь 400 г\nлук 2 шт\nзира, барбарис\nзаливать кипятком",
        "Дни рождения" to "мама — 3 марта\nАндрей — 17 июня\nКатя — 28 ноября",
        "Тренировка" to "пн: спина\nср: ноги\nпт: грудь + руки\nрастяжка каждый день",
        "Задачи по дому" to "поменять фильтр воды\nвызвать электрика\nотдать куртку в химчистку\nвернуть книгу",
        "Пароль от роутера" to "admin / admin\nадрес 192.168.1.1",
        "Такси в аэропорт" to "выезжать в 5:40\nтерминал B\nрегистрация закрывается за 40 минут",
        "Идеи подарков" to "наушники\nсертификат в книжный\nтермокружка\nнастольная игра",
        "Показания счётчиков" to "холодная 00412\nгорячая 00287\nэлектричество 15840",
        "Заметка" to "перезвонить в понедельник после 11\nуточнить по срокам",
        "Отпуск" to "загранпаспорт до 2029\nвзять переходник\nскачать карты офлайн\nстраховка",
        "Список дел" to "оплатить интернет\nзаписаться к врачу\nзабрать посылку\nответить на письмо"
    )

    fun generate(vaultId: String): List<Note> {
        val random = Random(seedFrom(vaultId))
        val indices = POOL.indices.toMutableList()
        // Перемешивание, зависящее только от идентификатора хранилища
        for (i in indices.size - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val tmp = indices[i]; indices[i] = indices[j]; indices[j] = tmp
        }
        val count = 4 + random.nextInt(4) // от 4 до 7 заметок
        val now = System.currentTimeMillis()
        return indices.take(count).map { index ->
            val (title, body) = POOL[index]
            val ageDays = 1 + random.nextInt(400)
            Note(
                id = UUID.randomUUID().toString(),
                title = title,
                body = body,
                updatedAt = now - TimeUnit.DAYS.toMillis(ageDays.toLong()) -
                        random.nextInt(20 * 60 * 60 * 1000)
            )
        }.sortedByDescending { it.updatedAt }
    }

    private fun seedFrom(vaultId: String): Long {
        var seed = 0L
        for (ch in vaultId) seed = seed * 31 + ch.code
        return seed
    }
}
