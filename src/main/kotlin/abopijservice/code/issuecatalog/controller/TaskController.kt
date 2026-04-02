package abopijservice.code.issuecatalog.controller

import abopijservice.code.issuecatalog.dto.PageResponse
import abopijservice.code.issuecatalog.dto.TaskRequest
import abopijservice.code.issuecatalog.dto.TaskResponse
import abopijservice.code.issuecatalog.dto.UpdateStatusRequest
import abopijservice.code.issuecatalog.model.TaskStatus
import abopijservice.code.issuecatalog.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/tasks")
class TaskController(private val taskService: TaskService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTask(@Valid @RequestBody request: TaskRequest): Mono<TaskResponse> =
        taskService.createTask(request.title, request.description)

    @GetMapping
    fun getTasks(
        @RequestParam page: Int,
        @RequestParam size: Int,
        @RequestParam(required = false) status: TaskStatus?
    ): Mono<PageResponse<TaskResponse>> =
        taskService.getTasks(page, size, status)

    @GetMapping("/{id}")
    fun getTaskById(@PathVariable id: Long): Mono<TaskResponse> =
        taskService.getTaskById(id)

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateStatusRequest
    ): Mono<TaskResponse> =
        taskService.updateStatus(id, request.status)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTask(@PathVariable id: Long): Mono<Void> =
        taskService.deleteTask(id)
}
