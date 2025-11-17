package com.example.studybuddy.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studybuddy.data.Task
import java.time.LocalDate

class CalendarViewModel : ViewModel() {

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    init {
        // Example: Load tasks for specific dates
        _tasks.value = listOf(
            Task(1, "Prepare for QUIZ 1", LocalDate.now()),
            Task(2, "Study for midterms", LocalDate.now().plusDays(2)),
            Task(3, "Final exam preparation", LocalDate.now().plusDays(5)),
            Task(4, "Finish Assignment 1", LocalDate.now().minusDays(3))
        )
    }
}