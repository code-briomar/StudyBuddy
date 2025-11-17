package com.example.studybuddy.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Study Buddy"
        val message = intent.getStringExtra("message") ?: "Time to study!"
        val notificationId = intent.getIntExtra("notification_id", 0)

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(title, message, NotificationHelper.REMINDER_CHANNEL_ID, notificationId)
    }
}
