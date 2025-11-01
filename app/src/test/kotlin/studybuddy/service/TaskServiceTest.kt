package studybuddy.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import studybuddy.model.Task
import java.time.LocalDateTime

class TaskServiceTest {
    
    private lateinit var taskService: TaskService
    
    @BeforeEach
    fun setUp() {
        taskService = TaskService()
    }
    
    @Test
    fun createTask() {
        val task = taskService.createTask(
            title = "Learn Kotlin",
            description = "Study basics",
            subject = "Programming",
            priority = Task.Priority.HIGH
        )
        
        assertNotNull(task.id)
        assertEquals("Learn Kotlin", task.title)
        assertEquals("Programming", task.subject)
        assertEquals(Task.Priority.HIGH, task.priority)
        
        val allTasks = taskService.getAllTasks()
        assertEquals(1, allTasks.size)
        assertTrue(allTasks.contains(task))
    }
    
    @Test
    fun getTaskById() {
        val task = taskService.createTask(
            title = "Study Math",
            subject = "Mathematics"
        )
        
        val retrievedTask = taskService.getTaskById(task.id)
        assertNotNull(retrievedTask)
        assertEquals(task.id, retrievedTask?.id)
        assertEquals("Study Math", retrievedTask?.title)
    }
    
    @Test
    fun getTasksBySubject() {
        taskService.createTask(title = "Task 1", subject = "Math")
        taskService.createTask(title = "Task 2", subject = "Physics")
        taskService.createTask(title = "Task 3", subject = "Math")
        
        val mathTasks = taskService.getTasksBySubject("Math")
        assertEquals(2, mathTasks.size)
        assertTrue(mathTasks.all { it.subject.equals("Math", ignoreCase = true) })
    }
    
    @Test
    fun getTasksByStatus() {
        val task1 = taskService.createTask(title = "Task 1", subject = "Test")
        taskService.createTask(title = "Task 2", subject = "Test")
        taskService.updateTaskStatus(task1.id, Task.TaskStatus.COMPLETED)
        
        val todoTasks = taskService.getTasksByStatus(Task.TaskStatus.TODO)
        assertEquals(1, todoTasks.size)
        
        val completedTasks = taskService.getTasksByStatus(Task.TaskStatus.COMPLETED)
        assertEquals(1, completedTasks.size)
    }
    
    @Test
    fun getPendingTasks() {
        val task1 = taskService.createTask(title = "Task 1", subject = "Test")
        val task2 = taskService.createTask(title = "Task 2", subject = "Test")
        taskService.createTask(title = "Task 3", subject = "Test")
        
        taskService.updateTaskStatus(task1.id, Task.TaskStatus.COMPLETED)
        taskService.updateTaskStatus(task2.id, Task.TaskStatus.CANCELLED)
        
        val pendingTasks = taskService.getPendingTasks()
        assertEquals(1, pendingTasks.size)
    }
    
    @Test
    fun getOverdueTasks() {
        val pastDeadline = LocalDateTime.now().minusDays(1)
        val futureDeadline = LocalDateTime.now().plusDays(1)
        
        taskService.createTask(
            title = "Overdue Task",
            subject = "Test",
            deadline = pastDeadline
        )
        taskService.createTask(
            title = "Future Task",
            subject = "Test",
            deadline = futureDeadline
        )
        
        val overdueTasks = taskService.getOverdueTasks()
        assertEquals(1, overdueTasks.size)
        assertEquals("Overdue Task", overdueTasks[0].title)
    }
    
    @Test
    fun updateTaskStatus() {
        val task = taskService.createTask(title = "Test Task", subject = "Test")
        
        val updated = taskService.updateTaskStatus(task.id, Task.TaskStatus.IN_PROGRESS)
        assertNotNull(updated)
        assertEquals(Task.TaskStatus.IN_PROGRESS, updated?.status)
        
        val completed = taskService.updateTaskStatus(task.id, Task.TaskStatus.COMPLETED)
        assertNotNull(completed?.completedAt)
    }
    
    @Test
    fun deleteTask() {
        val task = taskService.createTask(title = "To Delete", subject = "Test")
        assertEquals(1, taskService.getAllTasks().size)
        
        val deleted = taskService.deleteTask(task.id)
        assertTrue(deleted)
        assertEquals(0, taskService.getAllTasks().size)
    }
    
    @Test
    fun getTasksSortedByPriority() {
        taskService.createTask(title = "Low", subject = "Test", priority = Task.Priority.LOW)
        taskService.createTask(title = "Urgent", subject = "Test", priority = Task.Priority.URGENT)
        taskService.createTask(title = "Medium", subject = "Test", priority = Task.Priority.MEDIUM)
        taskService.createTask(title = "High", subject = "Test", priority = Task.Priority.HIGH)
        
        val sorted = taskService.getTasksSortedByPriority()
        assertEquals("Urgent", sorted[0].title)
        assertEquals("High", sorted[1].title)
        assertEquals("Medium", sorted[2].title)
        assertEquals("Low", sorted[3].title)
    }
    
    @Test
    fun getTasksSortedByDeadline() {
        val now = LocalDateTime.now()
        taskService.createTask(
            title = "Later", 
            subject = "Test", 
            deadline = now.plusDays(5)
        )
        taskService.createTask(
            title = "Soon", 
            subject = "Test", 
            deadline = now.plusDays(1)
        )
        taskService.createTask(
            title = "No deadline", 
            subject = "Test"
        )
        
        val sorted = taskService.getTasksSortedByDeadline()
        assertEquals("Soon", sorted[0].title)
        assertEquals("Later", sorted[1].title)
        assertEquals("No deadline", sorted[2].title)
    }
}
