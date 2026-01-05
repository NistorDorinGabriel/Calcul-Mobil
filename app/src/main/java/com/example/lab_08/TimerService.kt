package com.example.lab_08


import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class TimerService : Service() {

    companion object {
        const val ACTION_TICK = "com.example.soundservice.TIMER_TICK"
        const val EXTRA_SECONDS = "seconds"
        const val ACTION_STOP = "com.example.soundservice.TIMER_STOP"
    }

    private val handler = Handler(Looper.getMainLooper())
    private var seconds = 0
    private var running = false

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (!running) return

            seconds += 1

            // trimite timpul către Activity
            val intent = Intent(ACTION_TICK).apply {
                setPackage(packageName)
                putExtra(EXTRA_SECONDS, seconds)
            }
            sendBroadcast(intent)

            handler.postDelayed(this, 1000)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // dacă primești ACTION_STOP, te oprești
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (!running) {
            running = true
            handler.post(tickRunnable)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        running = false
        handler.removeCallbacks(tickRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
