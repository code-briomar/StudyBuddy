package com.example.studybuddy.ui.timer

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.studybuddy.R
import com.example.studybuddy.StudyBuddyApplication
import com.example.studybuddy.database.DatabaseHelper
import com.example.studybuddy.services.StudyTimerService

class StudyTimerActivity : AppCompatActivity() {

    private lateinit var circularProgressView: CircularProgressView
    private lateinit var timerText: TextView
    private lateinit var courseNameText: TextView
    private lateinit var pauseResumeButton: Button
    private lateinit var stopButton: Button

    private var studyTimerService: StudyTimerService? = null
    private var isBound = false
    private var totalTimeInMillis: Long = 0
    private var courseId: Int = 0
    private var courseName: String = ""
    private var originalMinutes: Int = 0

    private val databaseHelper: DatabaseHelper by lazy {
        (application as StudyBuddyApplication).databaseHelper
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as StudyTimerService.StudyTimerBinder
            studyTimerService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            studyTimerService = null
            isBound = false
        }
    }

    private val timerUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val timeLeft = intent?.getLongExtra(StudyTimerService.EXTRA_TIME_LEFT, 0) ?: 0
            updateTimerText(timeLeft)
            updateProgress(timeLeft)
        }
    }

    private val timerFinishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            saveStudySession(true)
            showCompletionDialog()
        }
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

        courseNameText.text = courseName

        val serviceIntent = Intent(this, StudyTimerService::class.java).apply {
            putExtra(StudyTimerService.EXTRA_MINUTES, minutes)
        }
        startService(serviceIntent)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Study Session"

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmation()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(timerUpdateReceiver, IntentFilter(StudyTimerService.ACTION_TIMER_UPDATE))
        LocalBroadcastManager.getInstance(this).registerReceiver(timerFinishReceiver, IntentFilter(StudyTimerService.ACTION_TIMER_FINISH))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerUpdateReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerFinishReceiver)
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
            studyTimerService?.let {
                if (it.isTimerRunning()) {
                    it.pauseTimer()
                    pauseResumeButton.text = "Resume"
                } else {
                    it.resumeTimer()
                    pauseResumeButton.text = "Pause"
                }
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

    private fun updateTimerText(timeLeftInMillis: Long) {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        timerText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateProgress(timeLeftInMillis: Long) {
        val progress = (timeLeftInMillis.toFloat() / totalTimeInMillis.toFloat()) * 100
        circularProgressView.setProgress(progress)
    }

    private fun saveStudySession(isCompleted: Boolean = false) {
        val studiedMinutes = if (isCompleted) {
            originalMinutes
        } else {
            studyTimerService?.let {
                ((totalTimeInMillis - it.getTimeLeftInMillis()) / 1000 / 60).toInt()
            } ?: 0
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
            .setMessage("Great job! You\'ve completed your study session.")
            .setPositiveButton("Finish") { _, _ ->
                stopAndUnbindService()
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
                saveStudySession()
                stopAndUnbindService()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun stopAndUnbindService() {
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        stopService(Intent(this, StudyTimerService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAndUnbindService()
    }
}