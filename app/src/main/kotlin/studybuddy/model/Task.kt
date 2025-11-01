package studybuddy.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents an academic task with priority, deadline, and status tracking
 */
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val subject: String,
    val priority: Priority = Priority.MEDIUM,
    val deadline: LocalDateTime? = null,
    val status: TaskStatus = TaskStatus.TODO,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null
) {
    enum class Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    enum class TaskStatus {
        TODO, IN_PROGRESS, COMPLETED, CANCELLED
    }
    
    fun markCompleted(): Task {
        return copy(
            status = TaskStatus.COMPLETED,
            completedAt = LocalDateTime.now()
        )
    }
    
    fun updateStatus(newStatus: TaskStatus): Task {
        return copy(status = newStatus)
    }
    
    fun isOverdue(): Boolean {
        return deadline?.let { 
            it.isBefore(LocalDateTime.now()) && status != TaskStatus.COMPLETED 
        } ?: false
    }
}
