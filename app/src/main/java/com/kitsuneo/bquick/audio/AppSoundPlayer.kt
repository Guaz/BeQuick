package com.kitsuneo.bquick.audio

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.net.Uri
import com.kitsuneo.bquick.settings.SoundSelection

object AppSoundPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var toneGenerator: ToneGenerator? = null

    fun play(context: Context, selection: SoundSelection) {
        when (selection) {
            is SoundSelection.BuiltIn -> {
                val generator = toneGenerator ?: ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100).also {
                    toneGenerator = it
                }
                generator.startTone(selection.sound.toneType, 250)
            }

            is SoundSelection.Custom -> {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(context, Uri.parse(selection.uri))?.apply {
                    setOnCompletionListener {
                        it.release()
                        if (mediaPlayer === it) {
                            mediaPlayer = null
                        }
                    }
                    start()
                }
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        toneGenerator?.release()
        toneGenerator = null
    }
}
