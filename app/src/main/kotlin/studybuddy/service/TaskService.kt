package studybuddy.service

import studybuddy.model.Task
import java.time.LocalDateTime

/**
 * Service for managing academic tasks
 */
class TaskService {
    private val tasks = mutableListOf<Task>()
    
    fun createTask(
        title: String,
        description: String = "",
        subject: String,
        priority: Task.Priority = Task.Priority.MEDIUM,
        deadline: LocalDateTime? = null
    ): Task {
        val task = Task(
            title = title,
            description = description,
            subject = subject,
            priority = priority,
            deadline = deadline
        )
        tasks.add(task)
        return task
    }
    
    fun getAllTasks(): List<Task> = tasks.toList()
    
    fun getTaskById(id: String): Task? = tasks.find { it.id == id }
    
    fun getTasksBySubject(subject: String): List<Task> = 
        tasks.filter { it.subject.equals(subject, ignoreCase = true) }
    
    fun getTasksByStatus(status: Task.TaskStatus): List<Task> = 
        tasks.filter { it.status == status }
    
    fun getPendingTasks(): List<Task> = 
        tasks.filter { it.status != Task.TaskStatus.COMPLETED && it.status != Task.TaskStatus.CANCELLED }
    
    fun getOverdueTasks(): List<Task> = 
        tasks.filter { it.isOverdue() }
    
    fun updateTaskStatus(id: String, status: Task.TaskStatus): Task? {
        val index = tasks.indexOfFirst { it.id == id }
        if (index != -1) {
            val updatedTask = if (status == Task.TaskStatus.COMPLETED) {
                tasks[index].markCompleted()
            } else {
                tasks[index].updateStatus(status)
            }
            tasks[index] = updatedTask
            return updatedTask
        }
        return null
    }
    
    fun deleteTask(id: String): Boolean {
        return tasks.removeIf { it.id == id }
    }
    
    fun getTasksSortedByPriority(): List<Task> {
        val priorityOrder = mapOf(
            Task.Priority.URGENT to 0,
            Task.Priority.HIGH to 1,
            Task.Priority.MEDIUM to 2,
            Task.Priority.LOW to 3
        )
        return tasks.sortedBy { priorityOrder[it.priority] }
    }
    
    fun getTasksSortedByDeadline(): List<Task> {
        return tasks.sortedWith(compareBy(nullsLast()) { it.deadline })
    }
}
