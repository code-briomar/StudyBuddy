package studybuddy.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime
import java.time.Duration

class StudySessionTest {
    
    @Test
    fun studySessionCreation() {
        val startTime = LocalDateTime.now()
        val session = StudySession(
            subject = "Mathematics",
            startTime = startTime
        )
        
        assertNotNull(session.id)
        assertEquals("Mathematics", session.subject)
        assertEquals(startTime, session.startTime)
        assertNull(session.endTime)
        assertTrue(session.isActive())
        assertTrue(session.productive)
    }
    
    @Test
    fun endStudySession() {
        val startTime = LocalDateTime.now().minusMinutes(30)
        val session = StudySession(
            subject = "Physics",
            startTime = startTime
        )
        
        val endedSession = session.end(notes = "Covered chapter 5", productive = true)
        
        assertNotNull(endedSession.endTime)
        assertEquals("Covered chapter 5", endedSession.notes)
        assertTrue(endedSession.productive)
        assertFalse(endedSession.isActive())
    }
    
    @Test
    fun calculateSessionDuration() {
        val startTime = LocalDateTime.now().minusMinutes(60)
        val endTime = LocalDateTime.now()
        val session = StudySession(
            subject = "Chemistry",
            startTime = startTime,
            endTime = endTime
        )
        
        val duration = session.duration()
        assertNotNull(duration)
        assertTrue(duration!!.toMinutes() >= 59) // Allow for small time differences
    }
    
    @Test
    fun activeSessionHasNoDuration() {
        val session = StudySession(
            subject = "Biology",
            startTime = LocalDateTime.now()
        )
        
        assertNull(session.duration())
    }
    
    @Test
    fun unproductiveSession() {
        val startTime = LocalDateTime.now().minusMinutes(15)
        val session = StudySession(
            subject = "History",
            startTime = startTime
        )
        
        val endedSession = session.end(notes = "Too distracted", productive = false)
        
        assertFalse(endedSession.productive)
        assertEquals("Too distracted", endedSession.notes)
    }
}
