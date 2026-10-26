package com.kartaviy.tgagent.tg

import org.drinkless.tdlib.TdApi

fun contentType(c: TdApi.MessageContent): String = when (c) {
    is TdApi.MessageText -> "text"
    is TdApi.MessagePhoto -> "photo"
    is TdApi.MessageVideo -> "video"
    is TdApi.MessageDocument -> "document"
    is TdApi.MessageVoiceNote -> "voice"
    is TdApi.MessageAudio -> "audio"
    is TdApi.MessageAnimation -> "gif"
    is TdApi.MessageSticker -> "sticker"
    is TdApi.MessageVideoNote -> "video_note"
    else -> "other"
}

private fun formatted(c: TdApi.MessageContent): TdApi.FormattedText? = when (c) {
    is TdApi.MessageText -> c.text
    is TdApi.MessagePhoto -> c.caption
    is TdApi.MessageVideo -> c.caption
    is TdApi.MessageDocument -> c.caption
    is TdApi.MessageVoiceNote -> c.caption
    is TdApi.MessageAudio -> c.caption
    is TdApi.MessageAnimation -> c.caption
    else -> null
}

/** Текст сообщения или подпись к медиа (плюс имя файла для документов). */
fun contentText(c: TdApi.MessageContent): String {
    val text = formatted(c)?.text.orEmpty()
    val extra = when (c) {
        is TdApi.MessageDocument -> c.document?.fileName
        is TdApi.MessageAudio -> c.audio?.title
        is TdApi.MessageSticker -> c.sticker?.emoji
        else -> null
    }
    return listOfNotNull(text.ifBlank { null }, extra?.ifBlank { null }).joinToString(" · ")
}

fun hasLink(c: TdApi.MessageContent): Boolean {
    val ft = formatted(c) ?: return false
    return ft.entities.orEmpty().any { it.type is TdApi.TextEntityTypeUrl || it.type is TdApi.TextEntityTypeTextUrl }
}

fun formattedText(text: String) = TdApi.FormattedText(text, arrayOf())
