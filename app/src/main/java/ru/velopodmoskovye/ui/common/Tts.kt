package ru.velopodmoskovye.ui.common

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/** Озвучка рассказа гида системным TTS (если в телефоне есть русский голос). */
class TtsController {
    internal var engine: TextToSpeech? = null
    var ready by mutableStateOf(false)
        internal set
    var speaking by mutableStateOf(false)
        internal set

    fun speak(paragraphs: List<String>) {
        val e = engine ?: return
        e.stop()
        paragraphs.forEachIndexed { i, p ->
            e.speak(p, if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, "p$i")
        }
        speaking = true
    }

    fun stop() {
        engine?.stop()
        speaking = false
    }
}

@Composable
fun rememberTts(): TtsController {
    val context = LocalContext.current
    val controller = remember { TtsController() }
    DisposableEffect(Unit) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val r = tts?.setLanguage(Locale("ru", "RU"))
                controller.ready = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
        controller.engine = tts
        onDispose {
            tts?.stop()
            tts?.shutdown()
            controller.engine = null
        }
    }
    return controller
}
