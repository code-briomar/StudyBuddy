package com.example.studybuddy.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R
import com.example.studybuddy.StudyBuddyApplication
import com.example.studybuddy.adapters.AssignmentAdapter
import com.example.studybuddy.adapters.StudySessionAdapter
import com.example.studybuddy.database.DatabaseHelper
import com.example.studybuddy.models.StudySession
import com.example.studybuddy.notification.NotificationHelper
import com.example.studybuddy.ui.assignments.ManageAssignmentsActivity


class DashboardActivity : AppCompatActivity() {

    private lateinit var totalStudyTime: TextView
    private lateinit var currentStreak: TextView
    private lateinit var weeklyProgress: TextView
    private lateinit var upcomingAssignmentsCount: TextView
    private lateinit var recentSessionsRecyclerView: RecyclerView
    private lateinit var upcomingAssignmentsRecyclerView: RecyclerView
    private lateinit var manageAssignmentsButton: Button
    private lateinit var startStudySessionButton: Button

    private lateinit var studySessionAdapter: StudySessionAdapter
    private lateinit var assignmentAdapter: AssignmentAdapter
    private val databaseHelper: DatabaseHelper by lazy { (application as StudyBuddyApplication).databaseHelper }
    private val notificationHelper: NotificationHelper by lazy { NotificationHelper(this) }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle the permission grant or denial
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_dashboard)

        initializeViews()
        handleWindowInsets()
        setupRecyclerViews()
        loadDashboardData()

        notificationHelper.showWelcomeNotification()

        manageAssignmentsButton.setOnClickListener {
            val intent = Intent(this, ManageAssignmentsActivity::class.java)
            startActivity(intent)
        }

        startStudySessionButton.setOnClickListener {
            showStartStudySessionDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        requestNotificationPermission()
        loadDashboardData() // Refresh data when returning to activity
    }

    private fun initializeViews() {
        totalStudyTime = findViewById(R.id.totalStudyTime)
        currentStreak = findViewById(R.id.currentStreak)
        weeklyProgress = findViewById(R.id.weeklyProgress)
        upcomingAssignmentsCount = findViewById(R.id.upcomingAssignmentsCount)
        recentSessionsRecyclerView = findViewById(R.id.recentSessionsRecyclerView)
        upcomingAssignmentsRecyclerView = findViewById(R.id.upcomingAssignmentsRecyclerView)
        manageAssignmentsButton = findViewById(R.id.manageAssignmentsButton)
        startStudySessionButton = findViewById(R.id.startStudySessionButton)
    }

    private fun handleWindowInsets() {
        val mainContent = findViewById<android.view.View>(R.id.main_content)
        ViewCompat.setOnApplyWindowInsetsListener(mainContent) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerViews() {
        // Study Sessions RecyclerView
        studySessionAdapter = StudySessionAdapter(emptyList(), databaseHelper)
        recentSessionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = studySessionAdapter
        }

        // Assignments RecyclerView
        assignmentAdapter = AssignmentAdapter(
            emptyList(),
            databaseHelper,
            onCheckboxClicked = { assignment ->
                val newCompletionState = !assignment.isCompleted
                showCompletionConfirmationDialog(assignment, newCompletionState)
            })
        upcomingAssignmentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = assignmentAdapter
        }
    }

    private fun loadDashboardData() {
        val sessions = databaseHelper.getAllStudySessions()
        val totalMinutes = sessions.sumOf { it.durationMinutes }
        val streak = databaseHelper.calculateCurrentStreak()
        val incompleteAssignments = databaseHelper.getUpcomingAssignments()

        updateTotalStudyTime(totalMinutes)
        updateCurrentStreak(streak)
        updateWeeklyProgress(totalMinutes)
        updateUpcomingAssignmentsCount(incompleteAssignments)
        updateRecentSessions(sessions)
        updateUpcomingAssignments(incompleteAssignments)
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

    private fun updateUpcomingAssignmentsCount(assignments: List<com.example.studybuddy.models.Assignment>) {
        val now = System.currentTimeMillis()

        val upcomingCount = assignments.count { it.dueDate.time >= now }
        val overdueCount = assignments.count { it.dueDate.time < now }

        upcomingAssignmentsCount.text = when {
            overdueCount > 0 && upcomingCount > 0 -> "$upcomingCount upcoming, $overdueCount overdue"
            overdueCount > 0 -> "$overdueCount overdue"
            upcomingCount > 0 -> "$upcomingCount upcoming"
            else -> "No assignments"
        }
    }

    private fun updateRecentSessions(sessions: List<StudySession>) {
        studySessionAdapter.updateSessions(sessions)
    }

    private fun updateUpcomingAssignments(assignments: List<com.example.studybuddy.models.Assignment>) {
        assignmentAdapter.updateAssignments(assignments)
    }

    private fun showCompletionConfirmationDialog(assignment: com.example.studybuddy.models.Assignment, isCompleted: Boolean) {
        val action = if (isCompleted) "completed" else "incomplete"
        val message = if (isCompleted) {
            "Mark \"${assignment.title}\" as completed?"
        } else {
            "Mark \"${assignment.title}\" as incomplete?"
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm")
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                databaseHelper.updateAssignmentCompletion(assignment.assignmentId, isCompleted)
                loadDashboardData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showStartStudySessionDialog() {
        val dialogView = android.view.LayoutInflater.from(this)
            .inflate(R.layout.dialog_start_study_session, null)

        val courseSpinner = dialogView.findViewById<android.widget.Spinner>(R.id.courseSpinner)
        val customDurationInput = dialogView.findViewById<android.widget.EditText>(R.id.customDurationInput)
        val duration15 = dialogView.findViewById<Button>(R.id.duration15)
        val duration25 = dialogView.findViewById<Button>(R.id.duration30)
        val duration45 = dialogView.findViewById<Button>(R.id.duration45)
        val duration60 = dialogView.findViewById<Button>(R.id.duration60)

        val courses = databaseHelper.getAllCourses()
        val courseNames = courses.map { it.name }
        val courseAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            courseNames
        )
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = courseAdapter

        var selectedMinutes = 25

        duration15.setOnClickListener {
            selectedMinutes = 15
            customDurationInput.setText("")
        }
        duration25.setOnClickListener {
            selectedMinutes = 30
            customDurationInput.setText("")
        }
        duration45.setOnClickListener {
            selectedMinutes = 45
            customDurationInput.setText("")
        }
        duration60.setOnClickListener {
            selectedMinutes = 60
            customDurationInput.setText("")
        }

        AlertDialog.Builder(this)
            .setTitle("Start Study Session")
            .setView(dialogView)
            .setPositiveButton("Start") { _, _ ->
                val customDuration = customDurationInput.text.toString()
                val finalMinutes = if (customDuration.isNotEmpty()) {
                    customDuration.toIntOrNull() ?: selectedMinutes
                } else {
                    selectedMinutes
                }

                val selectedCourse = courses[courseSpinner.selectedItemPosition]

                val intent = Intent(this, com.example.studybuddy.ui.timer.StudyTimerActivity::class.java)
                intent.putExtra("STUDY_MINUTES", finalMinutes)
                intent.putExtra("COURSE_ID", selectedCourse.courseId)
                intent.putExtra("COURSE_NAME", selectedCourse.name)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}