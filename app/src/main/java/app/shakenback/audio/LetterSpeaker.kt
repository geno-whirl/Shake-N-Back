package app.shakenback.audio

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Speaks the audio stimulus with the system text-to-speech engine, so the app
 * ships no audio assets. [ready] is null while the engine initialises.
 */
class LetterSpeaker(context: Context) : TextToSpeech.OnInitListener {

    private val _ready = MutableStateFlow<Boolean?>(null)
    val ready: StateFlow<Boolean?> = _ready.asStateFlow()

    private val tts = TextToSpeech(context.applicationContext, this)

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            _ready.value = false
            return
        }
        val language = tts.setLanguage(Locale.US)
        tts.setSpeechRate(1.1f)
        tts.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
        )
        _ready.value = language != TextToSpeech.LANG_MISSING_DATA &&
            language != TextToSpeech.LANG_NOT_SUPPORTED
    }

    fun speak(letter: Char) {
        if (_ready.value == true) {
            tts.speak(letter.toString(), TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
        }
    }

    fun stop() {
        tts.stop()
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }

    private companion object {
        const val UTTERANCE_ID = "stimulus"
    }
}
