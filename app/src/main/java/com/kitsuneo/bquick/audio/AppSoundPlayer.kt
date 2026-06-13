package com.kitsuneo.bquick.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.kitsuneo.bquick.settings.SoundSelection

object AppSoundPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun play(context: Context, selection: SoundSelection) {
        mediaPlayer?.release()
        mediaPlayer = when (selection) {
            is SoundSelection.BuiltIn -> MediaPlayer.create(context, selection.sound.rawResId)
            is SoundSelection.Custom -> MediaPlayer.create(context, Uri.parse(selection.uri))
        }?.apply {
            setOnCompletionListener {
                it.release()
                if (mediaPlayer === it) {
                    mediaPlayer = null
                }
            }
            start()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
