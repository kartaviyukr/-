package com.simple.notes.data

/** Одно найденное вхождение запроса: в заголовке или в тексте заметки. */
data class Match(val inTitle: Boolean, val start: Int, val end: Int)

/**
 * Поиск внутри одной заметки.
 *
 * Ищется любая подстрока: часть слова, отдельные символы, куски с пробелами.
 * Регистр не учитывается, «ё» и «е» считаются одной буквой.
 */
object NoteSearch {

    fun find(title: String, body: String, query: String): List<Match> {
        if (query.isBlank()) return emptyList()
        return matchesIn(title, query, inTitle = true) + matchesIn(body, query, inTitle = false)
    }

    private fun matchesIn(text: String, query: String, inTitle: Boolean): List<Match> {
        val haystack = normalize(text)
        val needle = normalize(query)
        if (needle.isEmpty() || needle.length > haystack.length) return emptyList()

        val found = mutableListOf<Match>()
        var from = 0
        while (from <= haystack.length - needle.length) {
            val at = haystack.indexOf(needle, from, ignoreCase = true)
            if (at < 0) break
            found += Match(inTitle, at, at + needle.length)
            from = at + needle.length
        }
        return found
    }

    /** Замена посимвольная, поэтому позиции символов не сдвигаются. */
    private fun normalize(text: String): String = text.replace('ё', 'е').replace('Ё', 'Е')
}
