package com.example.studybuddy.models

import java.util.Date

data class Assignment(
    val assignmentId: Int = 0,
    val courseId: Int = 0,
    val title: String = "",
    val description: String? = null,
    val dueDate: Date = Date(),
    val isCompleted: Boolean = false,
    val createdDate: Date = Date()
)