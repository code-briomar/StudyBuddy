package com.example.studybuddy.ui.timer

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.studybuddy.R
import com.example.studybuddy.StudyBuddyApplication
import com.example.studybuddy.database.DatabaseHelper
import com.example.studybuddy.ui.timer.CircularProgressView
import java.util.Date

class StudyTimerActivity : AppCompatActivity() {

    private lateinit var circularProgressView: CircularProgressView
    private lateinit var timerText: TextView
    private lateinit var courseNameText: TextView
    private lateinit var pauseResumeButton: Button
    private lateinit var stopButton: Button

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var totalTimeInMillis: Long = 0
    private var isTimerRunning = false
    private var courseId: Int = 0
    private var courseName: String = ""
    private var originalMinutes: Int = 0

    private val databaseHelper: DatabaseHelper by lazy {
        (application as StudyBuddyApplication).databaseHelper
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_study_timer)

        initializeViews()
        handleWindowInsets()

        val minutes = intent.getIntExtra("STUDY_MINUTES", 30)
        courseId = intent.getIntExtra("COURSE_ID", 1)
        courseName = intent.getStringExtra("COURSE_NAME") ?: "Study Session"

        originalMinutes = minutes
        totalTimeInMillis = minutes * 60 * 1000L
        timeLeftInMillis = totalTimeInMillis

        courseNameText.text = courseName
        updateTimerText()
        circularProgressView.setProgress(100f)

        startTimer()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Study Session"

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmation()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        showExitConfirmation()
        return true
    }

    private fun initializeViews() {
        circularProgressView = findViewById(R.id.circularProgressView)
        timerText = findViewById(R.id.timerText)
        courseNameText = findViewById(R.id.courseNameText)
        pauseResumeButton = findViewById(R.id.pauseResumeButton)
        stopButton = findViewById(R.id.stopButton)

        pauseResumeButton.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                resumeTimer()
            }
        }

        stopButton.setOnClickListener {
            showExitConfirmation()
        }
    }

    private fun handleWindowInsets() {
        val mainContent = findViewById<android.view.View>(R.id.main_content)
        ViewCompat.setOnApplyWindowInsetsListener(mainContent) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
                updateProgress()
            }

            override fun onFinish() {
                isTimerRunning = false
                saveStudySession(true) // Pass true to indicate completion
                showCompletionDialog()
            }
        }.start()

        isTimerRunning = true
        pauseResumeButton.text = "Pause"
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        pauseResumeButton.text = "Resume"
    }

    private fun resumeTimer() {
        startTimer()
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        timerText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateProgress() {
        val progress = (timeLeftInMillis.toFloat() / totalTimeInMillis.toFloat()) * 100
        circularProgressView.setProgress(progress)
    }

    private fun saveStudySession(isCompleted: Boolean = false) {
        // If timer completed fully, use the original minutes from intent
        // Otherwise calculate based on elapsed time
        val studiedMinutes = if (isCompleted) {
            originalMinutes
        } else {
            ((totalTimeInMillis - timeLeftInMillis) / 1000 / 60).toInt()
        }

        if (studiedMinutes > 0) {
            val db = databaseHelper.writableDatabase
            val values = android.content.ContentValues().apply {
                put(DatabaseHelper.COLUMN_COURSE_ID, courseId)
                put(DatabaseHelper.COLUMN_START_TIME, System.currentTimeMillis() - (studiedMinutes * 60 * 1000))
                put(DatabaseHelper.COLUMN_END_TIME, System.currentTimeMillis())
                put(DatabaseHelper.COLUMN_DURATION_MINUTES, studiedMinutes)
                put(DatabaseHelper.COLUMN_DATE, System.currentTimeMillis())
            }
            db.insert(DatabaseHelper.TABLE_STUDY_SESSIONS, null, values)
        }
    }

    private fun showCompletionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Session Complete!")
            .setMessage("Great job! You've completed your study session.")
            .setPositiveButton("Finish") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Stop Session?")
            .setMessage("Do you want to stop your study session? Your progress will be saved.")
            .setPositiveButton("Stop") { _, _ ->
                pauseTimer()
                saveStudySession()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}