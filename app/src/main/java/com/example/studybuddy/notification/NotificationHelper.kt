package com.example.studybuddy.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.studybuddy.R

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val REMINDER_CHANNEL_ID = "study_buddy_reminders"
        private const val REMINDER_CHANNEL_NAME = "Study Reminders"
        const val TIMER_CHANNEL_ID = "study_timer_channel"
        private const val TIMER_CHANNEL_NAME = "Study Timer"
        const val WELCOME_CHANNEL_ID = "study_buddy_welcome"
        private const val WELCOME_CHANNEL_NAME = "Welcome"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val timerChannel = NotificationChannel(
                TIMER_CHANNEL_ID,
                TIMER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Use low importance for timer notifications
            )

            val welcomeChannel = NotificationChannel(
                WELCOME_CHANNEL_ID,
                WELCOME_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannels(listOf(reminderChannel, timerChannel, welcomeChannel))
        }
    }

    fun showNotification(title: String, message: String, channelId: String, notificationId: Int) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(notificationId, builder.build())
    }

    fun showWelcomeNotification() {
        showNotification("Welcome to StudyBuddy!", "Let's get started on your academic journey.", WELCOME_CHANNEL_ID, 3)
    }
}
