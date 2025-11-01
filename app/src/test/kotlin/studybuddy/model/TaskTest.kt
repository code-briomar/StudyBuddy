package studybuddy.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class TaskTest {
    
    @Test
    fun taskCreationWithDefaults() {
        val task = Task(
            title = "Study Kotlin",
            subject = "Computer Science"
        )
        
        assertNotNull(task.id)
        assertEquals("Study Kotlin", task.title)
        assertEquals("Computer Science", task.subject)
        assertEquals(Task.Priority.MEDIUM, task.priority)
        assertEquals(Task.TaskStatus.TODO, task.status)
        assertNull(task.deadline)
        assertNull(task.completedAt)
    }
    
    @Test
    fun taskCreationWithAllFields() {
        val deadline = LocalDateTime.now().plusDays(2)
        val task = Task(
            title = "Complete Assignment",
            description = "Math homework chapter 5",
            subject = "Mathematics",
            priority = Task.Priority.HIGH,
            deadline = deadline
        )
        
        assertEquals("Complete Assignment", task.title)
        assertEquals("Math homework chapter 5", task.description)
        assertEquals("Mathematics", task.subject)
        assertEquals(Task.Priority.HIGH, task.priority)
        assertEquals(deadline, task.deadline)
    }
    
    @Test
    fun markTaskCompleted() {
        val task = Task(
            title = "Read Chapter 3",
            subject = "History"
        )
        
        val completedTask = task.markCompleted()
        
        assertEquals(Task.TaskStatus.COMPLETED, completedTask.status)
        assertNotNull(completedTask.completedAt)
    }
    
    @Test
    fun updateTaskStatus() {
        val task = Task(
            title = "Write Essay",
            subject = "English"
        )
        
        val inProgressTask = task.updateStatus(Task.TaskStatus.IN_PROGRESS)
        assertEquals(Task.TaskStatus.IN_PROGRESS, inProgressTask.status)
        
        val completedTask = inProgressTask.updateStatus(Task.TaskStatus.COMPLETED)
        assertEquals(Task.TaskStatus.COMPLETED, completedTask.status)
    }
    
    @Test
    fun taskIsOverdueWhenDeadlinePassed() {
        val pastDeadline = LocalDateTime.now().minusDays(1)
        val task = Task(
            title = "Overdue Task",
            subject = "Test",
            deadline = pastDeadline
        )
        
        assertTrue(task.isOverdue())
    }
    
    @Test
    fun taskIsNotOverdueWhenDeadlineNotPassed() {
        val futureDeadline = LocalDateTime.now().plusDays(1)
        val task = Task(
            title = "Future Task",
            subject = "Test",
            deadline = futureDeadline
        )
        
        assertFalse(task.isOverdue())
    }
    
    @Test
    fun completedTaskIsNotOverdue() {
        val pastDeadline = LocalDateTime.now().minusDays(1)
        val task = Task(
            title = "Completed Task",
            subject = "Test",
            deadline = pastDeadline,
            status = Task.TaskStatus.COMPLETED
        )
        
        assertFalse(task.isOverdue())
    }
    
    @Test
    fun taskWithoutDeadlineIsNotOverdue() {
        val task = Task(
            title = "No Deadline Task",
            subject = "Test"
        )
        
        assertFalse(task.isOverdue())
    }
}
