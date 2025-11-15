package com.example.studybuddy.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R
import com.example.studybuddy.database.DatabaseHelper
import com.example.studybuddy.models.StudySession
import java.text.SimpleDateFormat
import java.util.Locale

class StudySessionAdapter(
    private var studySessions: List<StudySession> = emptyList(),
    private val databaseHelper: DatabaseHelper
) : RecyclerView.Adapter<StudySessionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sessionCourse: TextView = view.findViewById(R.id.sessionCourse)
        val sessionDuration: TextView = view.findViewById(R.id.sessionDuration)
        val sessionDate: TextView = view.findViewById(R.id.sessionDate)
        val courseColorView: View = view.findViewById(R.id.courseColorView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_study_session, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = studySessions[position]

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        val course = databaseHelper.getCourseById(session.courseId)
        val courseName = course?.name ?: "Unknown Course"
        val courseColor = course?.colorCode?.let { Color.parseColor(it) } ?: Color.LTGRAY

        holder.sessionCourse.text = courseName
        holder.sessionDuration.text = "${session.durationMinutes} minutes"
        holder.sessionDate.text = dateFormat.format(session.date)
        
        val background = holder.courseColorView.background as GradientDrawable
        background.setColor(courseColor)
    }

    override fun getItemCount(): Int = studySessions.size

    fun updateSessions(newSessions: List<StudySession>) {
        studySessions = newSessions
        notifyDataSetChanged()
    }
}