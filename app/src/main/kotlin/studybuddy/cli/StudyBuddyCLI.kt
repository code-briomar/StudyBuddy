package studybuddy.cli

import studybuddy.model.Task
import studybuddy.service.TaskService
import studybuddy.service.StudySessionService
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Command-line interface for StudyBuddy application
 */
class StudyBuddyCLI {
    private val taskService = TaskService()
    private val sessionService = StudySessionService()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    
    fun start() {
        println("====================================")
        println("   Welcome to StudyBuddy 📚")
        println("   Your Academic Task Companion")
        println("====================================")
        println()
        
        var running = true
        while (running) {
            displayMenu()
            print("\nEnter your choice: ")
            when (readLine()?.trim()) {
                "1" -> createTask()
                "2" -> listTasks()
                "3" -> updateTaskStatus()
                "4" -> deleteTask()
                "5" -> startStudySession()
                "6" -> endStudySession()
                "7" -> viewStudyStatistics()
                "8" -> viewOverdueTasks()
                "9" -> {
                    running = false
                    println("\nThank you for using StudyBuddy! Keep up the great work! 🎓")
                }
                else -> println("\nInvalid choice. Please try again.")
            }
        }
    }
    
    private fun displayMenu() {
        println("\n====================================")
        println("Main Menu:")
        println("====================================")
        println("1. Create a new task")
        println("2. List all tasks")
        println("3. Update task status")
        println("4. Delete a task")
        println("5. Start study session")
        println("6. End study session")
        println("7. View study statistics")
        println("8. View overdue tasks")
        println("9. Exit")
    }
    
    private fun createTask() {
        println("\n--- Create New Task ---")
        print("Title: ")
        val title = readLine()?.trim() ?: ""
        if (title.isEmpty()) {
            println("Title cannot be empty.")
            return
        }
        
        print("Description (optional): ")
        val description = readLine()?.trim() ?: ""
        
        print("Subject: ")
        val subject = readLine()?.trim() ?: ""
        if (subject.isEmpty()) {
            println("Subject cannot be empty.")
            return
        }
        
        print("Priority (LOW/MEDIUM/HIGH/URGENT) [MEDIUM]: ")
        val priorityInput = readLine()?.trim()?.uppercase() ?: "MEDIUM"
        val priority = try {
            Task.Priority.valueOf(priorityInput)
        } catch (e: IllegalArgumentException) {
            Task.Priority.MEDIUM
        }
        
        print("Deadline (yyyy-MM-dd HH:mm) [optional]: ")
        val deadlineInput = readLine()?.trim()
        val deadline = if (!deadlineInput.isNullOrEmpty()) {
            try {
                LocalDateTime.parse(deadlineInput, dateFormatter)
            } catch (e: Exception) {
                println("Invalid date format. Task created without deadline.")
                null
            }
        } else null
        
        val task = taskService.createTask(title, description, subject, priority, deadline)
        println("\n✓ Task created successfully!")
        println("ID: ${task.id}")
        println("Title: ${task.title}")
        println("Subject: ${task.subject}")
        println("Priority: ${task.priority}")
        if (task.deadline != null) {
            println("Deadline: ${task.deadline.format(dateFormatter)}")
        }
    }
    
    private fun listTasks() {
        println("\n--- All Tasks ---")
        val tasks = taskService.getAllTasks()
        
        if (tasks.isEmpty()) {
            println("No tasks found. Create your first task!")
            return
        }
        
        println("\nFiltering options:")
        println("1. All tasks")
        println("2. By status")
        println("3. By subject")
        println("4. Sort by priority")
        println("5. Sort by deadline")
        print("\nChoose filter [1]: ")
        
        val filter = readLine()?.trim() ?: "1"
        val filteredTasks = when (filter) {
            "2" -> {
                print("Enter status (TODO/IN_PROGRESS/COMPLETED/CANCELLED): ")
                val statusInput = readLine()?.trim()?.uppercase()
                try {
                    val status = Task.TaskStatus.valueOf(statusInput ?: "TODO")
                    taskService.getTasksByStatus(status)
                } catch (e: IllegalArgumentException) {
                    tasks
                }
            }
            "3" -> {
                print("Enter subject: ")
                val subject = readLine()?.trim() ?: ""
                taskService.getTasksBySubject(subject)
            }
            "4" -> taskService.getTasksSortedByPriority()
            "5" -> taskService.getTasksSortedByDeadline()
            else -> tasks
        }
        
        if (filteredTasks.isEmpty()) {
            println("No tasks found with the selected filter.")
            return
        }
        
        println("\n" + "=".repeat(80))
        filteredTasks.forEach { task ->
            displayTask(task)
            println("=".repeat(80))
        }
        println("\nTotal: ${filteredTasks.size} task(s)")
    }
    
    private fun displayTask(task: Task) {
        val statusIcon = when (task.status) {
            Task.TaskStatus.TODO -> "☐"
            Task.TaskStatus.IN_PROGRESS -> "◐"
            Task.TaskStatus.COMPLETED -> "✓"
            Task.TaskStatus.CANCELLED -> "✗"
        }
        
        println("$statusIcon [${task.id.substring(0, 8)}] ${task.title}")
        println("   Subject: ${task.subject} | Priority: ${task.priority} | Status: ${task.status}")
        if (task.description.isNotEmpty()) {
            println("   Description: ${task.description}")
        }
        if (task.deadline != null) {
            val overdueMarker = if (task.isOverdue()) " ⚠️ OVERDUE" else ""
            println("   Deadline: ${task.deadline.format(dateFormatter)}$overdueMarker")
        }
        println("   Created: ${task.createdAt.format(dateFormatter)}")
        if (task.completedAt != null) {
            println("   Completed: ${task.completedAt.format(dateFormatter)}")
        }
    }
    
    private fun updateTaskStatus() {
        println("\n--- Update Task Status ---")
        val tasks = taskService.getPendingTasks()
        
        if (tasks.isEmpty()) {
            println("No pending tasks found.")
            return
        }
        
        println("Pending tasks:")
        tasks.forEachIndexed { index, task ->
            println("${index + 1}. [${task.id.substring(0, 8)}] ${task.title} (${task.status})")
        }
        
        print("\nSelect task number: ")
        val selection = readLine()?.trim()?.toIntOrNull()
        if (selection == null || selection < 1 || selection > tasks.size) {
            println("Invalid selection.")
            return
        }
        
        val task = tasks[selection - 1]
        println("\nCurrent status: ${task.status}")
        println("New status options:")
        println("1. TODO")
        println("2. IN_PROGRESS")
        println("3. COMPLETED")
        println("4. CANCELLED")
        
        print("\nSelect new status: ")
        val statusChoice = readLine()?.trim()
        val newStatus = when (statusChoice) {
            "1" -> Task.TaskStatus.TODO
            "2" -> Task.TaskStatus.IN_PROGRESS
            "3" -> Task.TaskStatus.COMPLETED
            "4" -> Task.TaskStatus.CANCELLED
            else -> {
                println("Invalid status choice.")
                return
            }
        }
        
        taskService.updateTaskStatus(task.id, newStatus)
        println("\n✓ Task status updated to $newStatus")
    }
    
    private fun deleteTask() {
        println("\n--- Delete Task ---")
        val tasks = taskService.getAllTasks()
        
        if (tasks.isEmpty()) {
            println("No tasks found.")
            return
        }
        
        tasks.forEachIndexed { index, task ->
            println("${index + 1}. [${task.id.substring(0, 8)}] ${task.title}")
        }
        
        print("\nSelect task number to delete: ")
        val selection = readLine()?.trim()?.toIntOrNull()
        if (selection == null || selection < 1 || selection > tasks.size) {
            println("Invalid selection.")
            return
        }
        
        val task = tasks[selection - 1]
        print("Are you sure you want to delete '${task.title}'? (yes/no): ")
        val confirmation = readLine()?.trim()?.lowercase()
        
        if (confirmation == "yes" || confirmation == "y") {
            taskService.deleteTask(task.id)
            println("\n✓ Task deleted successfully.")
        } else {
            println("\nDeletion cancelled.")
        }
    }
    
    private fun startStudySession() {
        println("\n--- Start Study Session ---")
        
        val activeSession = sessionService.getActiveSession()
        if (activeSession != null) {
            println("You already have an active study session for ${activeSession.subject}.")
            println("Please end it before starting a new one.")
            return
        }
        
        print("Subject: ")
        val subject = readLine()?.trim() ?: ""
        if (subject.isEmpty()) {
            println("Subject cannot be empty.")
            return
        }
        
        val session = sessionService.startSession(subject)
        println("\n✓ Study session started!")
        println("Session ID: ${session.id.substring(0, 8)}")
        println("Subject: ${session.subject}")
        println("Start time: ${session.startTime.format(dateFormatter)}")
        println("\nFocus on your studies! 📖")
    }
    
    private fun endStudySession() {
        println("\n--- End Study Session ---")
        
        val activeSession = sessionService.getActiveSession()
        if (activeSession == null) {
            println("No active study session found.")
            return
        }
        
        println("Active session: ${activeSession.subject}")
        println("Started at: ${activeSession.startTime.format(dateFormatter)}")
        
        print("\nNotes (optional): ")
        val notes = readLine()?.trim() ?: ""
        
        print("Was this session productive? (yes/no) [yes]: ")
        val productiveInput = readLine()?.trim()?.lowercase() ?: "yes"
        val productive = productiveInput != "no" && productiveInput != "n"
        
        val endedSession = sessionService.endSession(activeSession.id, notes, productive)
        if (endedSession != null) {
            val duration = endedSession.duration()
            println("\n✓ Study session ended!")
            println("Duration: ${formatDuration(duration)}")
            if (notes.isNotEmpty()) {
                println("Notes: $notes")
            }
            println("Productive: ${if (productive) "Yes ✓" else "No"}")
        }
    }
    
    private fun viewStudyStatistics() {
        println("\n--- Study Statistics ---")
        
        val allSessions = sessionService.getAllSessions()
        val completedSessions = allSessions.filter { it.endTime != null }
        
        if (completedSessions.isEmpty()) {
            println("No completed study sessions yet. Start your first session!")
            return
        }
        
        println("\nTotal Sessions: ${completedSessions.size}")
        println("Total Study Time: ${formatDuration(sessionService.getTotalStudyTime())}")
        
        val todaySessions = sessionService.getSessionsToday().filter { it.endTime != null }
        println("\nToday's Sessions: ${todaySessions.size}")
        
        val productiveSessions = sessionService.getProductiveSessions()
        val productivityRate = (productiveSessions.size.toDouble() / completedSessions.size * 100).toInt()
        println("Productive Sessions: ${productiveSessions.size} (${productivityRate}%)")
        
        println("\nStudy time by subject:")
        val subjects = completedSessions.map { it.subject }.distinct()
        subjects.forEach { subject ->
            val time = sessionService.getTotalStudyTimeBySubject(subject)
            println("  • $subject: ${formatDuration(time)}")
        }
        
        val activeSession = sessionService.getActiveSession()
        if (activeSession != null) {
            println("\n⚠️ Active session: ${activeSession.subject}")
            println("   Started at: ${activeSession.startTime.format(dateFormatter)}")
        }
    }
    
    private fun viewOverdueTasks() {
        println("\n--- Overdue Tasks ---")
        val overdueTasks = taskService.getOverdueTasks()
        
        if (overdueTasks.isEmpty()) {
            println("No overdue tasks. Great job keeping up! 🎉")
            return
        }
        
        println("\n⚠️ You have ${overdueTasks.size} overdue task(s):\n")
        println("=".repeat(80))
        overdueTasks.forEach { task ->
            displayTask(task)
            println("=".repeat(80))
        }
    }
    
    private fun formatDuration(duration: Duration?): String {
        if (duration == null) return "0h 0m"
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        return "${hours}h ${minutes}m"
    }
}
