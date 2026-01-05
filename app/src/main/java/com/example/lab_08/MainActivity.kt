package com.example.lab_08

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var timerText: TextView

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == TimerService.ACTION_TICK) {
                val seconds = intent.getIntExtra(TimerService.EXTRA_SECONDS, 0)
                timerText.text = formatSeconds(seconds)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playSoundButton: Button = findViewById(R.id.playSoundButton)
        playSoundButton.setOnClickListener {
            startService(Intent(this, SoundService::class.java))
        }

        timerText = findViewById(R.id.timerText)

        val startTimerButton: Button = findViewById(R.id.startTimerButton)
        val stopTimerButton: Button = findViewById(R.id.stopTimerButton)

        startTimerButton.setOnClickListener {
            startService(Intent(this, TimerService::class.java))
        }

        stopTimerButton.setOnClickListener {
            val i = Intent(this, TimerService::class.java).apply {
                action = TimerService.ACTION_STOP
            }
            startService(i) // trimite semnal de stop
        }
    }

    override fun onStart() {
        super.onStart()

        val filter = IntentFilter(TimerService.ACTION_TICK)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timerReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(timerReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(timerReceiver)
    }

    private fun formatSeconds(total: Int): String {
        val m = total / 60
        val s = total % 60
        return String.format("%02d:%02d", m, s)
    }
}