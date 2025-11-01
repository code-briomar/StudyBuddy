package studybuddy.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.time.Duration

class StudySessionServiceTest {
    
    private lateinit var sessionService: StudySessionService
    
    @BeforeEach
    fun setUp() {
        sessionService = StudySessionService()
    }
    
    @Test
    fun startSession() {
        val session = sessionService.startSession("Mathematics")
        
        assertNotNull(session.id)
        assertEquals("Mathematics", session.subject)
        assertTrue(session.isActive())
    }
    
    @Test
    fun endSession() {
        val session = sessionService.startSession("Physics")
        
        // Small delay to ensure duration is measurable
        Thread.sleep(100)
        
        val endedSession = sessionService.endSession(
            session.id, 
            notes = "Completed chapter 1",
            productive = true
        )
        
        assertNotNull(endedSession)
        assertFalse(endedSession!!.isActive())
        assertEquals("Completed chapter 1", endedSession.notes)
        assertTrue(endedSession.productive)
    }
    
    @Test
    fun getActiveSession() {
        assertNull(sessionService.getActiveSession())
        
        val session = sessionService.startSession("Chemistry")
        
        val activeSession = sessionService.getActiveSession()
        assertNotNull(activeSession)
        assertEquals(session.id, activeSession?.id)
        
        sessionService.endSession(session.id)
        assertNull(sessionService.getActiveSession())
    }
    
    @Test
    fun getSessionsBySubject() {
        sessionService.startSession("Math")
        val physicsSession = sessionService.startSession("Physics")
        sessionService.endSession(physicsSession.id)
        sessionService.startSession("Math")
        
        val mathSessions = sessionService.getSessionsBySubject("Math")
        assertEquals(2, mathSessions.size)
        assertTrue(mathSessions.all { it.subject.equals("Math", ignoreCase = true) })
    }
    
    @Test
    fun getTotalStudyTime() {
        val session1 = sessionService.startSession("Math")
        Thread.sleep(100)
        sessionService.endSession(session1.id)
        
        val session2 = sessionService.startSession("Physics")
        Thread.sleep(100)
        sessionService.endSession(session2.id)
        
        val totalTime = sessionService.getTotalStudyTime()
        assertTrue(totalTime.toMillis() >= 200)
    }
    
    @Test
    fun getTotalStudyTimeBySubject() {
        val math1 = sessionService.startSession("Math")
        Thread.sleep(100)
        sessionService.endSession(math1.id)
        
        val physics = sessionService.startSession("Physics")
        Thread.sleep(50)
        sessionService.endSession(physics.id)
        
        val math2 = sessionService.startSession("Math")
        Thread.sleep(100)
        sessionService.endSession(math2.id)
        
        val mathTime = sessionService.getTotalStudyTimeBySubject("Math")
        assertTrue(mathTime.toMillis() >= 200)
        
        val physicsTime = sessionService.getTotalStudyTimeBySubject("Physics")
        assertTrue(physicsTime.toMillis() >= 50)
        assertTrue(mathTime > physicsTime)
    }
    
    @Test
    fun getSessionsToday() {
        sessionService.startSession("History")
        val session = sessionService.startSession("Geography")
        sessionService.endSession(session.id)
        
        val todaySessions = sessionService.getSessionsToday()
        assertEquals(2, todaySessions.size)
    }
    
    @Test
    fun getProductiveSessions() {
        val productive = sessionService.startSession("Math")
        sessionService.endSession(productive.id, productive = true)
        
        val unproductive = sessionService.startSession("Physics")
        sessionService.endSession(unproductive.id, productive = false)
        
        // Active session should not be counted
        sessionService.startSession("Chemistry")
        
        val productiveSessions = sessionService.getProductiveSessions()
        assertEquals(1, productiveSessions.size)
        assertEquals("Math", productiveSessions[0].subject)
    }
    
    @Test
    fun cannotEndSessionTwice() {
        val session = sessionService.startSession("Biology")
        
        val firstEnd = sessionService.endSession(session.id)
        assertNotNull(firstEnd)
        
        val secondEnd = sessionService.endSession(session.id)
        assertNull(secondEnd)
    }
    
    @Test
    fun totalStudyTimeWithNoSessions() {
        val totalTime = sessionService.getTotalStudyTime()
        assertEquals(Duration.ZERO, totalTime)
    }
    
    @Test
    fun totalStudyTimeWithActiveSession() {
        sessionService.startSession("Active Session")
        
        // Active sessions should not contribute to total time
        val totalTime = sessionService.getTotalStudyTime()
        assertEquals(Duration.ZERO, totalTime)
    }
}
