package com.example.studybuddy.ui.assignments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R
import com.example.studybuddy.StudyBuddyApplication
import com.example.studybuddy.adapters.AssignmentAdapter
import com.example.studybuddy.database.DatabaseHelper
import com.example.studybuddy.models.Assignment
import com.example.studybuddy.models.Course
import com.example.studybuddy.notification.NotificationScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ManageAssignmentsActivity : AppCompatActivity() {

    private lateinit var allAssignmentsRecyclerView: RecyclerView
    private lateinit var assignmentCountText: TextView
    private lateinit var addAssignmentFab: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var assignmentAdapter: AssignmentAdapter
    private val databaseHelper: DatabaseHelper by lazy { (application as StudyBuddyApplication).databaseHelper }
    private val notificationScheduler: NotificationScheduler by lazy { NotificationScheduler(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_manage_assignments)

        initializeViews()
        handleWindowInsets()
        setupRecyclerView()
        loadAllAssignments()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Manage Assignments"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initializeViews() {
        allAssignmentsRecyclerView = findViewById(R.id.allAssignmentsRecyclerView)
        assignmentCountText = findViewById(R.id.assignmentCountText)
        addAssignmentFab = findViewById(R.id.addAssignmentFab)

        addAssignmentFab.setOnClickListener {
            showAddAssignmentDialog()
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

    private fun setupRecyclerView() {
        assignmentAdapter = AssignmentAdapter(
            emptyList(),
            databaseHelper,
            onCheckboxClicked = { assignment ->
                val newCompletionState = !assignment.isCompleted
                showCompletionConfirmationDialog(assignment, newCompletionState)
            },
            onAssignmentLongClick = { assignment ->
                showAssignmentOptionsMenu(assignment)
            }
        )
        allAssignmentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ManageAssignmentsActivity)
            adapter = assignmentAdapter
        }
    }

    private fun loadAllAssignments() {
        val assignments = databaseHelper.getAllAssignments()
        assignmentAdapter.updateAssignments(assignments)

        val completedCount = assignments.count { it.isCompleted }
        val totalCount = assignments.size
        assignmentCountText.text = "$completedCount of $totalCount completed"
    }

    private fun showCompletionConfirmationDialog(assignment: com.example.studybuddy.models.Assignment, isCompleted: Boolean) {
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
                if (isCompleted) {
                    notificationScheduler.cancelAssignmentReminder(assignment.assignmentId)
                } else {
                    databaseHelper.getAssignmentById(assignment.assignmentId)?.let {
                        notificationScheduler.scheduleAssignmentReminder(it)
                    }
                }
                loadAllAssignments()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAssignmentOptionsMenu(assignment: Assignment) {
        val popupMenu = PopupMenu(this, allAssignmentsRecyclerView)
        popupMenu.menuInflater.inflate(R.menu.assignment_options_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    showEditAssignmentDialog(assignment)
                    true
                }
                R.id.menu_delete -> {
                    showDeleteConfirmationDialog(assignment)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showEditAssignmentDialog(assignment: Assignment) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_assignment, null)

        val titleInput = dialogView.findViewById<EditText>(R.id.editAssignmentTitle)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.editAssignmentDescription)
        val courseSpinner = dialogView.findViewById<Spinner>(R.id.editAssignmentCourseSpinner)
        val dueDateText = dialogView.findViewById<TextView>(R.id.editAssignmentDueDate)

        val courses = databaseHelper.getAllCourses()
        val courseNames = courses.map { it.name }
        val courseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courseNames)
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = courseAdapter

        titleInput.setText(assignment.title)
        descriptionInput.setText(assignment.description ?: "")

        val currentCourseIndex = courses.indexOfFirst { it.courseId == assignment.courseId }
        if (currentCourseIndex != -1) {
            courseSpinner.setSelection(currentCourseIndex)
        }

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        dueDateText.text = dateFormat.format(assignment.dueDate)

        var selectedDueDate = assignment.dueDate

        dueDateText.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = selectedDueDate

            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDueDate = calendar.time
                    dueDateText.text = dateFormat.format(selectedDueDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Assignment")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newTitle = titleInput.text.toString().trim()
                val newDescription = descriptionInput.text.toString().trim()
                val selectedCourse = courses[courseSpinner.selectedItemPosition]

                if (newTitle.isNotEmpty()) {
                    databaseHelper.updateAssignment(
                        assignment.assignmentId,
                        newTitle,
                        newDescription,
                        selectedCourse.courseId,
                        selectedDueDate
                    )
                    databaseHelper.getAssignmentById(assignment.assignmentId)?.let {
                        notificationScheduler.scheduleAssignmentReminder(it)
                    }
                    loadAllAssignments()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(assignment: Assignment) {
        AlertDialog.Builder(this)
            .setTitle("Delete Assignment")
            .setMessage("Are you sure you want to delete \"${assignment.title}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                databaseHelper.deleteAssignment(assignment.assignmentId)
                notificationScheduler.cancelAssignmentReminder(assignment.assignmentId)
                loadAllAssignments()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddAssignmentDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_assignment, null)

        val titleInput = dialogView.findViewById<EditText>(R.id.addAssignmentTitle)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.addAssignmentDescription)
        val courseSpinner = dialogView.findViewById<Spinner>(R.id.addAssignmentCourseSpinner)
        val dueDateText = dialogView.findViewById<TextView>(R.id.addAssignmentDueDate)

        val courses = databaseHelper.getAllCourses()
        val courseNames = courses.map { it.name }
        val courseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courseNames)
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = courseAdapter

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7) // Default to 1 week from now
        var selectedDueDate = calendar.time
        dueDateText.text = dateFormat.format(selectedDueDate)

        dueDateText.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.time = selectedDueDate

            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    cal.set(year, month, dayOfMonth)
                    selectedDueDate = cal.time
                    dueDateText.text = dateFormat.format(selectedDueDate)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add Assignment")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()
                val selectedCourse = courses[courseSpinner.selectedItemPosition]

                if (title.isNotEmpty()) {
                    val newAssignmentId = databaseHelper.addAssignment(
                        title,
                        description,
                        selectedCourse.courseId,
                        selectedDueDate
                    )
                    databaseHelper.getAssignmentById(newAssignmentId.toInt())?.let {
                        notificationScheduler.scheduleAssignmentReminder(it)
                    }
                    loadAllAssignments()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}