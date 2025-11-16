package com.example.studybuddy

import android.app.Application
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.studybuddy.database.DatabaseHelper
import com.example.studybuddy.notification.ReminderWorker
import java.util.concurrent.TimeUnit

class StudyBuddyApplication : Application() {

    //Just one instance of the DB
    val databaseHelper by lazy { DatabaseHelper(this) }

    override fun onCreate() {
        super.onCreate()
        setupReminderWorker()
    }

    private fun setupReminderWorker() {
        val reminderWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(applicationContext).enqueue(reminderWorkRequest)
    }
}
