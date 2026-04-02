package abopijservice.code.issuecatalog.dto

import abopijservice.code.issuecatalog.model.TaskStatus
import jakarta.validation.constraints.NotNull

data class UpdateStatusRequest(
    @field:NotNull
    val status: TaskStatus
)
