package com.example.studybuddy.models

import java.util.Date

data class StudySession(
    val sessionId: Int = 0,
    val courseId: Int = 0,
    val startTime: Date = Date(),
    val endTime: Date = Date(),
    val durationMinutes: Int = 0,
    val date: Date = Date()
)