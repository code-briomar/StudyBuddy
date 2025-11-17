package com.example.studybuddy.models

import java.util.Date
import java.time.LocalDate
import java.time.LocalDateTime

data class StudySession(
    val sessionId: Int = 0,
    val courseId: Int = 0,
    val startTime: LocalDateTime = LocalDateTime.now(),
    val endTime: LocalDateTime = LocalDateTime.now(),
    val durationMinutes: Int = 0,
    val date: LocalDate = LocalDate.now()
)