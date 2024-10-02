package com.example.instantvoice.framework

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.instantvoice.R


class MyForegroundService : Service() {
    private val CHANNEL_ID = "VoiceMessagePlayerChannel"
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioFocusManager: AudioFocusManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        audioFocusManager = AudioFocusManager(this)

        createNotificationChannel()

        val url = intent?.getStringExtra("url")
        val name = intent?.getStringExtra("title")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(R.string.app_name.toString())
            .setContentText(name)
            .setSound(null)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setSmallIcon(R.drawable.play_icon)
            .build()
        startForeground(1, notification)

        if (url != null) {
            playToNotification(url)
        }

        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun playToNotification(url: String) {
        if (audioFocusManager.requestAudioFocus()) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setOnPreparedListener {
                    start()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("VoiceMessagePlayer", "Playback error: $what, $extra")
                    true
                }
                setOnCompletionListener {
                    stopForeground(true)
                    stopSelf()
                    audioFocusManager.abandonAudioFocus()
                }
                setDataSource(url)
                prepareAsync()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val soundUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.beep_sound)
//            val audioAttributes = AudioAttributes.Builder()
//                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//                .build()
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Voice Message Player Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
//                .apply {
//                setSound(soundUri, audioAttributes)
//            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        stopForeground(true)
        audioFocusManager.abandonAudioFocus()
    }
}
