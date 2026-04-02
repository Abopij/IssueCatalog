package abopijservice.code.issuecatalog.service

import abopijservice.code.issuecatalog.dto.PageResponse
import abopijservice.code.issuecatalog.dto.TaskResponse
import abopijservice.code.issuecatalog.dto.toResponse
import abopijservice.code.issuecatalog.exception.TaskNotFoundException
import abopijservice.code.issuecatalog.model.TaskStatus
import abopijservice.code.issuecatalog.repository.TaskRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import kotlin.math.ceil

@Service
class TaskService(private val repository: TaskRepository) {

    fun createTask(title: String, description: String?): Mono<TaskResponse> =
        Mono.fromCallable { repository.save(title, description).toResponse() }
            .subscribeOn(Schedulers.boundedElastic())

    fun getTaskById(id: Long): Mono<TaskResponse> =
        Mono.fromCallable { repository.findById(id) ?: throw TaskNotFoundException(id) }
            .map { it.toResponse() }
            .subscribeOn(Schedulers.boundedElastic())

    fun getTasks(page: Int, size: Int, status: TaskStatus?): Mono<PageResponse<TaskResponse>> =
        Mono.fromCallable {
            val tasks = repository.findAll(page, size, status)
            val total = repository.count(status)
            PageResponse(
                content = tasks.map { it.toResponse() },
                page = page,
                size = size,
                totalElements = total,
                totalPages = ceil(total.toDouble() / size).toInt()
            )
        }.subscribeOn(Schedulers.boundedElastic())

    fun updateStatus(id: Long, status: TaskStatus): Mono<TaskResponse> =
        Mono.fromCallable { repository.updateStatus(id, status) ?: throw TaskNotFoundException(id) }
            .map { it.toResponse() }
            .subscribeOn(Schedulers.boundedElastic())

    fun deleteTask(id: Long): Mono<Void> =
        Mono.fromCallable { if (!repository.deleteById(id)) throw TaskNotFoundException(id) }
            .subscribeOn(Schedulers.boundedElastic())
            .then()
}
