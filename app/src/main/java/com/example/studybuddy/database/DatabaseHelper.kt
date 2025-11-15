package com.example.studybuddy.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.studybuddy.models.Course
import com.example.studybuddy.models.StudySession
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "StudyBuddy.db"
        private const val DATABASE_VERSION = 1

        // Course Table
        const val TABLE_COURSES = "courses"
        const val COLUMN_COURSE_ID = "course_id"
        const val COLUMN_COURSE_NAME = "name"
        const val COLUMN_COLOR_CODE = "color_code"

        // Study Session Table
        const val TABLE_STUDY_SESSIONS = "study_sessions"
        const val COLUMN_SESSION_ID = "session_id"
        const val COLUMN_START_TIME = "start_time"
        const val COLUMN_END_TIME = "end_time"
        const val COLUMN_DURATION_MINUTES = "duration_minutes"
        const val COLUMN_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_COURSES_TABLE = """
            CREATE TABLE $TABLE_COURSES (
                $COLUMN_COURSE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_COURSE_NAME TEXT NOT NULL,
                $COLUMN_COLOR_CODE TEXT
            )
        """
        db.execSQL(CREATE_COURSES_TABLE)

        val CREATE_SESSIONS_TABLE = """
            CREATE TABLE $TABLE_STUDY_SESSIONS (
                $COLUMN_SESSION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_COURSE_ID INTEGER,
                $COLUMN_START_TIME INTEGER NOT NULL,
                $COLUMN_END_TIME INTEGER NOT NULL,
                $COLUMN_DURATION_MINUTES INTEGER NOT NULL,
                $COLUMN_DATE INTEGER NOT NULL,
                FOREIGN KEY($COLUMN_COURSE_ID) REFERENCES $TABLE_COURSES($COLUMN_COURSE_ID)
            )
        """
        db.execSQL(CREATE_SESSIONS_TABLE)

        insertSampleCourses(db)
        insertSampleStudySessions(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDY_SESSIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COURSES")
        onCreate(db)
    }

    private fun insertSampleCourses(db: SQLiteDatabase) {
        val courses = listOf(
            Course(courseId = 1, name = "Mathematics", colorCode = "#FF6B6B"),
            Course(courseId = 2, name = "Computer Science", colorCode = "#4ECDC4"),
            Course(courseId = 3, name = "Physics", colorCode = "#45B7D1"),
        )

        courses.forEach { course ->
            val values = ContentValues().apply {
                put(COLUMN_COURSE_NAME, course.name)
                put(COLUMN_COLOR_CODE, course.colorCode)
            }
            db.insert(TABLE_COURSES, null, values)
        }
    }

    private fun insertSampleStudySessions(db: SQLiteDatabase) {
        val sessions = listOf(
            StudySession(courseId = 1, durationMinutes = 120, date = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1))), // Yesterday
            StudySession(courseId = 2, durationMinutes = 90, date = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2))), // 2 days ago
            StudySession(courseId = 1, durationMinutes = 60, date = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(4)))  // 4 days ago
        )

        sessions.forEach { session ->
            val values = ContentValues().apply {
                put(COLUMN_COURSE_ID, session.courseId)
                put(COLUMN_START_TIME, session.startTime.time)
                put(COLUMN_END_TIME, session.endTime.time)
                put(COLUMN_DURATION_MINUTES, session.durationMinutes)
                put(COLUMN_DATE, session.date.time)
            }
            db.insert(TABLE_STUDY_SESSIONS, null, values)
        }
    }

    fun getAllCourses(): List<Course> {
        val courses = mutableListOf<Course>()
        val db = readableDatabase
        val cursor = db.query(TABLE_COURSES, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val course = Course(
                    courseId = it.getInt(it.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(COLUMN_COURSE_NAME)),
                    colorCode = it.getString(it.getColumnIndexOrThrow(COLUMN_COLOR_CODE))
                )
                courses.add(course)
            }
        }
        return courses
    }

    fun getCourseById(courseId: Int): Course? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_COURSES,
            null,
            "$COLUMN_COURSE_ID = ?",
            arrayOf(courseId.toString()),
            null, null, null
        )

        var course: Course? = null
        cursor.use {
            if (it.moveToFirst()) {
                course = Course(
                    courseId = it.getInt(it.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(COLUMN_COURSE_NAME)),
                    colorCode = it.getString(it.getColumnIndexOrThrow(COLUMN_COLOR_CODE))
                )
            }
        }
        return course
    }

    fun getAllStudySessions(): List<StudySession> {
        val sessions = mutableListOf<StudySession>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_STUDY_SESSIONS, null, null, null, null, null, "$COLUMN_DATE DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val session = StudySession(
                    sessionId = it.getInt(it.getColumnIndexOrThrow(COLUMN_SESSION_ID)),
                    courseId = it.getInt(it.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                    startTime = Date(it.getLong(it.getColumnIndexOrThrow(COLUMN_START_TIME))),
                    endTime = Date(it.getLong(it.getColumnIndexOrThrow(COLUMN_END_TIME))),
                    durationMinutes = it.getInt(it.getColumnIndexOrThrow(COLUMN_DURATION_MINUTES)),
                    date = Date(it.getLong(it.getColumnIndexOrThrow(COLUMN_DATE)))
                )
                sessions.add(session)
            }
        }
        return sessions
    }

    fun calculateCurrentStreak(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT $COLUMN_DATE FROM $TABLE_STUDY_SESSIONS ORDER BY $COLUMN_DATE DESC", null)

        val dates = mutableListOf<Long>()
        cursor.use {
            while(it.moveToNext()) {
                dates.add(it.getLong(0))
            }
        }

        if (dates.isEmpty()) return 0

        var streak = 0
        val today = Calendar.getInstance()
        val lastSessionDate = Calendar.getInstance().apply { timeInMillis = dates[0] }

        // Check if the most recent session was today or yesterday
        if (isSameDay(today, lastSessionDate) || isYesterday(today, lastSessionDate)) {
            streak = 1
            for (i in 0 until dates.size - 1) {
                val current = Calendar.getInstance().apply { timeInMillis = dates[i] }
                val previous = Calendar.getInstance().apply { timeInMillis = dates[i+1] }
                if (isYesterday(current, previous)) {
                    streak++
                } else {
                    break
                }
            }
        }
        
        return streak
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(today: Calendar, yesterday: Calendar): Boolean {
        val clone = today.clone() as Calendar
        clone.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(clone, yesterday)
    }
}