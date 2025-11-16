package com.example.studybuddy.adapters

import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R
import com.example.studybuddy.database.DatabaseHelper
import com.example.studybuddy.models.Assignment
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

// --- FIX #1: ADJUST THE CONSTRUCTOR PARAMETERS ---
class AssignmentAdapter(
    private var assignments: List<Assignment> = emptyList(),
    private val databaseHelper: DatabaseHelper,
    // This now correctly expects a lambda with a single `Assignment` parameter.
    private val onCheckboxClicked: ((Assignment) -> Unit)? = null,
    private val onAssignmentLongClick: ((Assignment) -> Unit)? = null
) : RecyclerView.Adapter<AssignmentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val assignmentTitle: TextView = view.findViewById(R.id.assignmentTitle)
        val assignmentCourse: TextView = view.findViewById(R.id.assignmentCourse)
        val assignmentDueDate: TextView = view.findViewById(R.id.assignmentDueDate)
        val courseColorView: View = view.findViewById(R.id.courseColorView)
        val completionCheckbox: CheckBox = view.findViewById(R.id.completionCheckbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_assignment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val assignment = assignments[position]

        val course = databaseHelper.getCourseById(assignment.courseId)
        val courseName = course?.name ?: "Unknown Course"
        val courseColor = course?.colorCode?.let { Color.parseColor(it) } ?: Color.LTGRAY

        holder.assignmentTitle.text = assignment.title
        holder.assignmentCourse.text = courseName
        holder.assignmentDueDate.text = formatDueDate(assignment.dueDate)

        val background = holder.courseColorView.background as GradientDrawable
        background.setColor(courseColor)

        // --- FIX #2: REMOVE REDUNDANT `isChecked` CALL ---
        // You set this again below after disabling the listener, which is the correct pattern.
        // holder.completionCheckbox.isChecked = assignment.isCompleted (This line is redundant)

        // Apply strikethrough if completed
        if (assignment.isCompleted) {
            holder.assignmentTitle.paintFlags = holder.assignmentTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.assignmentTitle.paintFlags = holder.assignmentTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        // Change due date color if overdue
        if (!assignment.isCompleted && assignment.dueDate.time < System.currentTimeMillis()) {
            holder.assignmentDueDate.setTextColor(Color.parseColor("#FF6B6B"))
        } else {
            // Revert color for items that are not overdue
            holder.assignmentDueDate.setTextColor(Color.parseColor("#666666"))
        }

        // A. Set the checkbox state without firing listeners
        holder.completionCheckbox.setOnCheckedChangeListener(null)
        holder.completionCheckbox.isChecked = assignment.isCompleted

        // B. Set the click listener ONLY on the checkbox
        holder.completionCheckbox.setOnClickListener {
            // Invoke the specific checkbox listener from the constructor
            onCheckboxClicked?.invoke(assignment)
        }

        // C. Make the main item view non-clickable to avoid confusion
        holder.itemView.isClickable = false
        holder.itemView.setOnClickListener(null)

        // D. The long-click listener remains on the entire item view
        holder.itemView.setOnLongClickListener {
            onAssignmentLongClick?.invoke(assignment)
            true // Consume the long-click event
        }
    }

    override fun getItemCount(): Int = assignments.size

    fun updateAssignments(newAssignments: List<Assignment>) {
        assignments = newAssignments
        notifyDataSetChanged()
    }

    private fun formatDueDate(dueDate: java.util.Date): String {
        val now = System.currentTimeMillis()
        val diff = dueDate.time - now
        val daysDiff = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            daysDiff < 0 -> {
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                "Overdue (${dateFormat.format(dueDate)})"
            }
            daysDiff == 0L -> "Due Today"
            daysDiff == 1L -> "Due Tomorrow"
            daysDiff < 7 -> "Due in $daysDiff days"
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                "Due ${dateFormat.format(dueDate)}"
            }
        }
    }
}
