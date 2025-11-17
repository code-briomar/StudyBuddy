package com.example.studybuddy.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.studybuddy.R
import com.example.studybuddy.notification.NotificationHelper
import com.example.studybuddy.ui.timer.StudyTimerActivity

class StudyTimerService : Service() {

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var isTimerRunning = false

    private val binder = StudyTimerBinder()

    inner class StudyTimerBinder : Binder() {
        fun getService(): StudyTimerService = this@StudyTimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val minutes = intent?.getIntExtra(EXTRA_MINUTES, 0) ?: 0
        timeLeftInMillis = minutes * 60 * 1000L

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                createNotification(formatTime(timeLeftInMillis)),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification(formatTime(timeLeftInMillis)))
        }
        startTimer()

        return START_NOT_STICKY
    }

    fun startTimer() {
        isTimerRunning = true
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateNotification(formatTime(timeLeftInMillis))
                broadcastTimerUpdate()
            }

            override fun onFinish() {
                isTimerRunning = false
                broadcastTimerFinish()
                stopSelf()
            }
        }.start()
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
    }

    fun resumeTimer() {
        startTimer()
    }

    fun isTimerRunning(): Boolean = isTimerRunning

    fun getTimeLeftInMillis(): Long = timeLeftInMillis

    private fun broadcastTimerUpdate() {
        val intent = Intent(ACTION_TIMER_UPDATE).apply {
            putExtra(EXTRA_TIME_LEFT, timeLeftInMillis)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastTimerFinish() {
        val intent = Intent(ACTION_TIMER_FINISH)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun updateNotification(time: String) {
        val notification = createNotification(time)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(time: String): Notification {
        val notificationIntent = Intent(this, StudyTimerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, NotificationHelper.TIMER_CHANNEL_ID)
            .setContentTitle("Study Session")
            .setContentText("Time remaining: $time")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun formatTime(millis: Long): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_MINUTES = "EXTRA_MINUTES"
        const val EXTRA_TIME_LEFT = "EXTRA_TIME_LEFT"
        const val ACTION_TIMER_UPDATE = "ACTION_TIMER_UPDATE"
        const val ACTION_TIMER_FINISH = "ACTION_TIMER_FINISH"
        private const val NOTIFICATION_ID = 2 // Use a different ID
    }
}
