package com.kitsuneo.bquick.audio

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import com.kitsuneo.bquick.settings.TimerSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object TimerSignalPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var toneGenerator: ToneGenerator? = null
    private var mediaPlayer: MediaPlayer? = null
    private var playbackJob: Job? = null

    fun play(context: Context, signal: TimerSignal) {
        playbackJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        val rawResId = signal.rawResId
        if (rawResId != null) {
            mediaPlayer = MediaPlayer.create(context.applicationContext, rawResId)?.apply {
                setOnCompletionListener {
                    it.release()
                    if (mediaPlayer === it) {
                        mediaPlayer = null
                    }
                }
                start()
            }
            return
        }
        val generator = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 90).also {
            toneGenerator = it
        }
        playbackJob = scope.launch {
            signal.pattern.forEach { step ->
                generator.startTone(step.toneType, step.durationMs)
                delay(step.durationMs.toLong() + step.pauseAfterMs)
            }
        }
    }

    fun release() {
        playbackJob?.cancel()
        playbackJob = null
        mediaPlayer?.release()
        mediaPlayer = null
        toneGenerator?.release()
        toneGenerator = null
    }

    fun shutdown() {
        release()
        scope.cancel()
    }
}
