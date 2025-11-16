package com.example.studybuddy.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.studybuddy.models.Assignment
import com.example.studybuddy.models.Course
import com.example.studybuddy.models.StudySession
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "StudyBuddy.db"
        private const val DATABASE_VERSION = 2 // Updated version

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

        // Assignment Table
        const val TABLE_ASSIGNMENTS = "assignments"
        const val COLUMN_ASSIGNMENT_ID = "assignment_id"
        const val COLUMN_ASSIGNMENT_TITLE = "title"
        const val COLUMN_ASSIGNMENT_DESCRIPTION = "description"
        const val COLUMN_DUE_DATE = "due_date"
        const val COLUMN_IS_COMPLETED = "is_completed"
        const val COLUMN_CREATED_DATE = "created_date"
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

        val CREATE_ASSIGNMENTS_TABLE = """
            CREATE TABLE $TABLE_ASSIGNMENTS (
                $COLUMN_ASSIGNMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_COURSE_ID INTEGER,
                $COLUMN_ASSIGNMENT_TITLE TEXT NOT NULL,
                $COLUMN_ASSIGNMENT_DESCRIPTION TEXT,
                $COLUMN_DUE_DATE INTEGER NOT NULL,
                $COLUMN_IS_COMPLETED INTEGER NOT NULL DEFAULT 0,
                $COLUMN_CREATED_DATE INTEGER NOT NULL,
                FOREIGN KEY($COLUMN_COURSE_ID) REFERENCES $TABLE_COURSES($COLUMN_COURSE_ID)
            )
        """
        db.execSQL(CREATE_ASSIGNMENTS_TABLE)

        insertSampleCourses(db)
        insertSampleStudySessions(db)
        insertSampleAssignments(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            val CREATE_ASSIGNMENTS_TABLE = """
                CREATE TABLE $TABLE_ASSIGNMENTS (
                    $COLUMN_ASSIGNMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_COURSE_ID INTEGER,
                    $COLUMN_ASSIGNMENT_TITLE TEXT NOT NULL,
                    $COLUMN_ASSIGNMENT_DESCRIPTION TEXT,
                    $COLUMN_DUE_DATE INTEGER NOT NULL,
                    $COLUMN_IS_COMPLETED INTEGER NOT NULL DEFAULT 0,
                    $COLUMN_CREATED_DATE INTEGER NOT NULL,
                    FOREIGN KEY($COLUMN_COURSE_ID) REFERENCES $TABLE_COURSES($COLUMN_COURSE_ID)
                )
            """
            db.execSQL(CREATE_ASSIGNMENTS_TABLE)
            insertSampleAssignments(db)
        }
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
            StudySession(courseId = 1, durationMinutes = 120, date = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1))),
            StudySession(courseId = 2, durationMinutes = 90, date = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2))),
            StudySession(courseId = 1, durationMinutes = 60, date = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(4)))
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

    private fun insertSampleAssignments(db: SQLiteDatabase) {
        val currentTime = System.currentTimeMillis()
        val assignments = listOf(
            Assignment(
                courseId = 1,
                title = "Calculus Problem Set 5",
                description = "Complete problems 1-20 from chapter 5",
                dueDate = Date(currentTime + TimeUnit.DAYS.toMillis(3)),
                isCompleted = false,
                createdDate = Date(currentTime)
            ),
            Assignment(
                courseId = 2,
                title = "Android App Project",
                description = "Build a simple calculator app",
                dueDate = Date(currentTime + TimeUnit.DAYS.toMillis(7)),
                isCompleted = false,
                createdDate = Date(currentTime)
            ),
            Assignment(
                courseId = 3,
                title = "Lab Report: Pendulum Motion",
                description = "Write up findings from last week's lab",
                dueDate = Date(currentTime + TimeUnit.DAYS.toMillis(2)),
                isCompleted = false,
                createdDate = Date(currentTime)
            ),
            Assignment(
                courseId = 1,
                title = "Midterm Study Guide",
                description = "Review chapters 1-5",
                dueDate = Date(currentTime - TimeUnit.DAYS.toMillis(1)),
                isCompleted = true,
                createdDate = Date(currentTime - TimeUnit.DAYS.toMillis(5))
            )
        )

        assignments.forEach { assignment ->
            val values = ContentValues().apply {
                put(COLUMN_COURSE_ID, assignment.courseId)
                put(COLUMN_ASSIGNMENT_TITLE, assignment.title)
                put(COLUMN_ASSIGNMENT_DESCRIPTION, assignment.description)
                put(COLUMN_DUE_DATE, assignment.dueDate.time)
                put(COLUMN_IS_COMPLETED, if (assignment.isCompleted) 1 else 0)
                put(COLUMN_CREATED_DATE, assignment.createdDate.time)
            }
            db.insert(TABLE_ASSIGNMENTS, null, values)
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

    fun getAllAssignments(): List<Assignment> {
        val assignments = mutableListOf<Assignment>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_ASSIGNMENTS, null, null, null, null, null, "$COLUMN_DUE_DATE ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val assignment = Assignment(
                    assignmentId = it.getInt(it.getColumnIndexOrThrow(COLUMN_ASSIGNMENT_ID)),
                    courseId = it.getInt(it.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                    title = it.getString(it.getColumnIndexOrThrow(COLUMN_ASSIGNMENT_TITLE)),
                    description = it.getString(it.getColumnIndexOrThrow(COLUMN_ASSIGNMENT_DESCRIPTION)),
                    dueDate = Date(it.getLong(it.getColumnIndexOrThrow(COLUMN_DUE_DATE))),
                    isCompleted = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1,
                    createdDate = Date(it.getLong(it.getColumnIndexOrThrow(COLUMN_CREATED_DATE)))
                )
                assignments.add(assignment)
            }
        }
        return assignments
    }

    fun getUpcomingAssignments(): List<Assignment> {
        val assignments = mutableListOf<Assignment>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_ASSIGNMENTS,
            null,
            "$COLUMN_IS_COMPLETED = ? AND $COLUMN_DUE_DATE >= ?",
            arrayOf("0", System.currentTimeMillis().toString()),
            null, null,
            "$COLUMN_DUE_DATE ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val assignment = Assignment(
                    assignmentId = it.getInt(it.getColumnIndexOrThrow(COLUMN_ASSIGNMENT_ID)),
                    courseId = it.getInt(it.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                    title = it.getString(it.getColumnIndexOrThrow(COLUMN_ASSIGNMENT_TITLE)),
                    description = it.getString(it.getColumnIndexOrThrow(COLUMN_ASSIGNMENT_DESCRIPTION)),
                    dueDate = Date(it.getLong(it.getColumnIndexOrThrow(COLUMN_DUE_DATE))),
                    isCompleted = false,
                    createdDate = Date(it.getLong(it.getColumnIndexOrThrow(COLUMN_CREATED_DATE)))
                )
                assignments.add(assignment)
            }
        }
        return assignments
    }

    fun updateAssignmentCompletion(assignmentId: Int, isCompleted: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        val rowsAffected = db.update(
            TABLE_ASSIGNMENTS,
            values,
            "$COLUMN_ASSIGNMENT_ID = ?",
            arrayOf(assignmentId.toString())
        )
        return rowsAffected > 0
    }

    fun updateAssignment(assignmentId: Int, title: String, description: String, courseId: Int, dueDate: Date): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ASSIGNMENT_TITLE, title)
            put(COLUMN_ASSIGNMENT_DESCRIPTION, description)
            put(COLUMN_COURSE_ID, courseId)
            put(COLUMN_DUE_DATE, dueDate.time)
        }
        val rowsAffected = db.update(
            TABLE_ASSIGNMENTS,
            values,
            "$COLUMN_ASSIGNMENT_ID = ?",
            arrayOf(assignmentId.toString())
        )
        return rowsAffected > 0
    }

    fun deleteAssignment(assignmentId: Int): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete(
            TABLE_ASSIGNMENTS,
            "$COLUMN_ASSIGNMENT_ID = ?",
            arrayOf(assignmentId.toString())
        )
        return rowsDeleted > 0
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