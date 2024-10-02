package com.example.instantvoice.framework

import android.media.MediaPlayer
import android.util.Log
import com.example.instantvoice.presentation.adapter.MessageAdapter

class VoiceMessagePlayer(private val adapter: MessageAdapter) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingPosition: Int? = null

    fun play(url: String, position: Int) {
        stop()
        currentPlayingPosition = position
        mediaPlayer = MediaPlayer().apply {
            setOnPreparedListener {
                start()
            }
            setOnCompletionListener {
                adapter.updatePlayingPosition(null)
                stop()
            }
            setOnErrorListener { _, what, extra ->
                Log.e("VoiceMessagePlayer", "Playback error: $what, $extra")
                true
            }
            setDataSource(url)
            prepareAsync()
        }
        adapter.updatePlayingPosition(position)
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentPlayingPosition?.let {
            adapter.updatePlayingPosition(null)
            currentPlayingPosition = null
        }
    }
}
