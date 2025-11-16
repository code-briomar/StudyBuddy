package com.example.studybuddy.logic

import com.example.studybuddy.database.DatabaseHelper

class ReminderManager(private val databaseHelper: DatabaseHelper) {

    fun getReminderMessage(): String? {
        val streak = databaseHelper.calculateCurrentStreak()
        val sessions = databaseHelper.getAllStudySessions()

        return if (sessions.isEmpty()) {
            "You haven't started studying yet. Let's begin!"
        } else if (streak == 0) {
            "It's been a while! Time to get back to studying and build your streak."
        } else if (streak > 0) {
            "You're on a ${streak}-day streak! Keep up the great work!"
        } else {
            null
        }
    }
}
