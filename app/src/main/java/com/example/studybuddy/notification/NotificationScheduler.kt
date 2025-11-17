
package com.example.studybuddy.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.studybuddy.models.Assignment
import java.util.Calendar

class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAssignmentReminder(assignment: Assignment) {
        val reminderTime = Calendar.getInstance().apply {
            time = assignment.dueDate
            add(Calendar.DAY_OF_YEAR, -1) // Remind 1 day before
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", "Assignment Due Tomorrow")
            putExtra("message", "Your assignment \"${assignment.title}\" is due tomorrow.")
            putExtra("notification_id", assignment.assignmentId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            assignment.assignmentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            reminderTime.timeInMillis,
            pendingIntent
        )
    }

    fun cancelAssignmentReminder(assignmentId: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            assignmentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
