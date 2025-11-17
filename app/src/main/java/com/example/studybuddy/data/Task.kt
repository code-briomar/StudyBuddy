package com.example.studybuddy.data

import java.time.LocalDate

data class Task(
    val id: Int,
    val title: String,
    val date: LocalDate
)