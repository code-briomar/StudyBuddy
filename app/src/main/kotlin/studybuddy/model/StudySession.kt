package studybuddy.model

import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents a study session to help track study habits
 */
data class StudySession(
    val id: String = UUID.randomUUID().toString(),
    val subject: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val notes: String = "",
    val productive: Boolean = true
) {
    fun duration(): Duration? {
        return endTime?.let { Duration.between(startTime, it) }
    }
    
    fun end(notes: String = "", productive: Boolean = true): StudySession {
        return copy(
            endTime = LocalDateTime.now(),
            notes = notes,
            productive = productive
        )
    }
    
    fun isActive(): Boolean = endTime == null
}
