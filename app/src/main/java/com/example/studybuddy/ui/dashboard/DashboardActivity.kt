package com.example.studybuddy.ui.dashboard

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R
import com.example.studybuddy.adapters.StudySessionAdapter
import com.example.studybuddy.database.DatabaseHelper
import com.example.studybuddy.models.StudySession

class DashboardActivity : AppCompatActivity() {

    private lateinit var totalStudyTime: TextView
    private lateinit var currentStreak: TextView
    private lateinit var weeklyProgress: TextView
    private lateinit var recentSessionsRecyclerView: RecyclerView

    private lateinit var studySessionAdapter: StudySessionAdapter
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_dashboard)

        databaseHelper = DatabaseHelper(this)

        initializeViews()
        handleWindowInsets()
        setupRecyclerView()
        loadDashboardData()
    }

    private fun initializeViews() {
        totalStudyTime = findViewById(R.id.totalStudyTime)
        currentStreak = findViewById(R.id.currentStreak)
        weeklyProgress = findViewById(R.id.weeklyProgress)
        recentSessionsRecyclerView = findViewById(R.id.recentSessionsRecyclerView)
    }

    private fun handleWindowInsets() {
        val mainContent = findViewById<android.view.View>(R.id.main_content)
        ViewCompat.setOnApplyWindowInsetsListener(mainContent) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        studySessionAdapter = StudySessionAdapter(emptyList(), databaseHelper)
        recentSessionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = studySessionAdapter
        }
    }

    private fun loadDashboardData() {
        val sessions = databaseHelper.getAllStudySessions()
        val totalMinutes = sessions.sumOf { it.durationMinutes }
        val streak = databaseHelper.calculateCurrentStreak()

        updateTotalStudyTime(totalMinutes)
        updateCurrentStreak(streak)
        updateWeeklyProgress(totalMinutes)
        updateRecentSessions(sessions)
    }

    private fun updateTotalStudyTime(totalMinutes: Int) {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        totalStudyTime.text = "${hours}h ${minutes}m"
    }

    private fun updateCurrentStreak(streak: Int) {
        currentStreak.text = "$streak days"
    }

    private fun updateWeeklyProgress(totalMinutes: Int) {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        weeklyProgress.text = "Studied ${hours}h ${minutes}m this week"
    }

    private fun updateRecentSessions(sessions: List<StudySession>) {
        studySessionAdapter.updateSessions(sessions)
    }
}