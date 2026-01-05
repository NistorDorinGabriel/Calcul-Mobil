package com.example.lab_08

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class SoundService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Amână redarea cu 3 secunde (ca în PDF)
        handler.postDelayed({
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.example)
            }
            mediaPlayer?.start()

            // Oprește serviciul după terminarea sunetului
            mediaPlayer?.setOnCompletionListener {
                stopSelf()
            }
        }, 3000)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}