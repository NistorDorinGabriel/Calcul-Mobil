package com.example.lab_08

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ForeSoundService : Service() {

    private val handler = Handler()
    private lateinit var mediaPlayer: MediaPlayer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Creează canal de notificare (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "SoundServiceChannel",
                "Sound Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        // Notificare pentru Foreground Service
        val notification: Notification = NotificationCompat.Builder(this, "SoundServiceChannel")
            .setContentTitle("Sound Service")
            .setContentText("Playing sound...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()

        startForeground(1, notification)

        // Redă sunetul după 3 secunde
        handler.postDelayed({
            mediaPlayer = MediaPlayer.create(this, R.raw.example) // fișierul din res/raw
            mediaPlayer.start()

            mediaPlayer.setOnCompletionListener {
                stopForeground(true) // elimină notificarea
                stopSelf()            // oprește serviciul
            }
        }, 3000)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}