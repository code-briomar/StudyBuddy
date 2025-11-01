package studybuddy.service

import studybuddy.model.StudySession
import java.time.Duration
import java.time.LocalDateTime

/**
 * Service for managing study sessions and tracking study habits
 */
class StudySessionService {
    private val sessions = mutableListOf<StudySession>()
    
    fun startSession(subject: String): StudySession {
        val session = StudySession(
            subject = subject,
            startTime = LocalDateTime.now()
        )
        sessions.add(session)
        return session
    }
    
    fun endSession(id: String, notes: String = "", productive: Boolean = true): StudySession? {
        val index = sessions.indexOfFirst { it.id == id }
        if (index != -1 && sessions[index].isActive()) {
            val endedSession = sessions[index].end(notes, productive)
            sessions[index] = endedSession
            return endedSession
        }
        return null
    }
    
    fun getAllSessions(): List<StudySession> = sessions.toList()
    
    fun getSessionsBySubject(subject: String): List<StudySession> =
        sessions.filter { it.subject.equals(subject, ignoreCase = true) }
    
    fun getActiveSession(): StudySession? = sessions.find { it.isActive() }
    
    fun getTotalStudyTime(): Duration {
        return sessions
            .mapNotNull { it.duration() }
            .fold(Duration.ZERO) { acc, duration -> acc.plus(duration) }
    }
    
    fun getTotalStudyTimeBySubject(subject: String): Duration {
        return sessions
            .filter { it.subject.equals(subject, ignoreCase = true) }
            .mapNotNull { it.duration() }
            .fold(Duration.ZERO) { acc, duration -> acc.plus(duration) }
    }
    
    fun getSessionsToday(): List<StudySession> {
        val today = LocalDateTime.now().toLocalDate()
        return sessions.filter { it.startTime.toLocalDate() == today }
    }
    
    fun getProductiveSessions(): List<StudySession> =
        sessions.filter { it.productive && it.endTime != null }
}
