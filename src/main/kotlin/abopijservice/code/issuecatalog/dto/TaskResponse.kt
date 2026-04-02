package abopijservice.code.issuecatalog.dto

import abopijservice.code.issuecatalog.model.Task
import abopijservice.code.issuecatalog.model.TaskStatus
import java.time.LocalDateTime

data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

fun Task.toResponse() = TaskResponse(id, title, description, status, createdAt, updatedAt)
