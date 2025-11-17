package com.example.studybuddy.ui.calender


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.studybuddy.StudyBuddyApplication
import com.example.studybuddy.model.Task // Assuming you have a Task data class
import java.time.LocalDate

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val databaseHelper = (application as StudyBuddyApplication).databaseHelper

    private val _tasks = MutableLiveData<Map<LocalDate, List<Task>>>()
    val tasks: LiveData<Map<LocalDate, List<Task>>> = _tasks

    init {
        loadAllTasks()
    }

    private fun loadAllTasks() {
        // Fetch all tasks from the database
        val allTasks = databaseHelper.getAllTasks() // You'll need to implement this method

        // Group tasks by their due date (LocalDate)
        // This assumes your Task model has a property that can be converted to LocalDate
        val tasksGroupedByDate = allTasks.groupBy { task ->
            task.dueDate // Assuming 'dueDate' is a LocalDate. Convert if it's a String/Long.
        }
        _tasks.postValue(tasksGroupedByDate)
    }
}
