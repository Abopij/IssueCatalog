package abopijservice.code.issuecatalog.model

import java.time.LocalDateTime

data class Task(
    val id: Long,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class TaskStatus {
    NEW, IN_PROGRESS, DONE, CANCELLED
}
