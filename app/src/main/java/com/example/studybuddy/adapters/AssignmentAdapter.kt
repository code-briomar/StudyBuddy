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

class AssignmentAdapter(
    private var assignments: List<Assignment> = emptyList(),
    private val databaseHelper: DatabaseHelper,
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

        // strikethrough for completed
        if (assignment.isCompleted) {
            holder.assignmentTitle.paintFlags = holder.assignmentTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.assignmentTitle.paintFlags = holder.assignmentTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        // red due date if overdue
        if (!assignment.isCompleted && assignment.dueDate.time < System.currentTimeMillis()) {
            holder.assignmentDueDate.setTextColor(Color.parseColor("#FF6B6B"))
        } else {
            holder.assignmentDueDate.setTextColor(Color.parseColor("#666666"))
        }

        holder.completionCheckbox.setOnCheckedChangeListener(null)
        holder.completionCheckbox.isChecked = assignment.isCompleted

        holder.completionCheckbox.setOnClickListener {
            onCheckboxClicked?.invoke(assignment)
        }

        holder.itemView.isClickable = false
        holder.itemView.setOnClickListener(null)

        holder.itemView.setOnLongClickListener {
            onAssignmentLongClick?.invoke(assignment)
            true
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
