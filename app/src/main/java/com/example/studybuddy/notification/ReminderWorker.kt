package com.example.studybuddy.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.studybuddy.StudyBuddyApplication
import com.example.studybuddy.logic.ReminderManager

class ReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val databaseHelper = (applicationContext as StudyBuddyApplication).databaseHelper
        val reminderManager = ReminderManager(databaseHelper)
        val notificationHelper = NotificationHelper(applicationContext)

        val message = reminderManager.getReminderMessage()
        if (message != null) {
            notificationHelper.showNotification("Study Buddy", message)
        }

        return Result.success()
    }
}
