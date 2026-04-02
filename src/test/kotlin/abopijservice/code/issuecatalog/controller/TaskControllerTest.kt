package abopijservice.code.issuecatalog.controller

import abopijservice.code.issuecatalog.dto.PageResponse
import abopijservice.code.issuecatalog.dto.TaskResponse
import abopijservice.code.issuecatalog.exception.TaskNotFoundException
import abopijservice.code.issuecatalog.model.TaskStatus
import abopijservice.code.issuecatalog.service.TaskService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@WebFluxTest(TaskController::class)
class TaskControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockitoBean
    private lateinit var taskService: TaskService

    private val now = LocalDateTime.now()
    private val taskResponse = TaskResponse(1L, "Test task", "description", TaskStatus.NEW, now, now)

    @Test
    fun `POST api tasks returns 201 with created task`() {
        whenever(taskService.createTask("Test task", "description")).thenReturn(Mono.just(taskResponse))

        webTestClient.post().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"title":"Test task","description":"description"}""")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.status").isEqualTo("NEW")
            .jsonPath("$.title").isEqualTo("Test task")
    }

    @Test
    fun `POST api tasks returns 400 for blank title`() {
        webTestClient.post().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"title":""}""")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `POST api tasks returns 400 for title shorter than 3 characters`() {
        webTestClient.post().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"title":"ab"}""")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `POST api tasks returns 400 for title longer than 100 characters`() {
        val longTitle = "a".repeat(101)
        webTestClient.post().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"title":"$longTitle"}""")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `GET api tasks id returns 200 with task`() {
        whenever(taskService.getTaskById(1L)).thenReturn(Mono.just(taskResponse))

        webTestClient.get().uri("/api/tasks/1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.title").isEqualTo("Test task")
    }

    @Test
    fun `GET api tasks id returns 404 when task not found`() {
        whenever(taskService.getTaskById(99L))
            .thenReturn(Mono.error(TaskNotFoundException(99L)))

        webTestClient.get().uri("/api/tasks/99")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.error").isEqualTo("Task not found: 99")
    }

    @Test
    fun `GET api tasks returns paginated list without status filter`() {
        val page = PageResponse(listOf(taskResponse), 0, 10, 1L, 1)
        whenever(taskService.getTasks(0, 10, null)).thenReturn(Mono.just(page))

        webTestClient.get().uri("/api/tasks?page=0&size=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.totalElements").isEqualTo(1)
            .jsonPath("$.page").isEqualTo(0)
            .jsonPath("$.size").isEqualTo(10)
            .jsonPath("$.content[0].id").isEqualTo(1)
    }

    @Test
    fun `GET api tasks returns paginated list with status filter`() {
        val page = PageResponse(listOf(taskResponse), 0, 10, 1L, 1)
        whenever(taskService.getTasks(0, 10, TaskStatus.NEW)).thenReturn(Mono.just(page))

        webTestClient.get().uri("/api/tasks?page=0&size=10&status=NEW")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.content[0].status").isEqualTo("NEW")
    }

    @Test
    fun `PATCH api tasks id status returns 200 with updated task`() {
        val updated = taskResponse.copy(status = TaskStatus.DONE)
        whenever(taskService.updateStatus(1L, TaskStatus.DONE)).thenReturn(Mono.just(updated))

        webTestClient.patch().uri("/api/tasks/1/status")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"status":"DONE"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("DONE")
    }

    @Test
    fun `PATCH api tasks id status returns 404 when task not found`() {
        whenever(taskService.updateStatus(99L, TaskStatus.DONE))
            .thenReturn(Mono.error(TaskNotFoundException(99L)))

        webTestClient.patch().uri("/api/tasks/99/status")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"status":"DONE"}""")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `DELETE api tasks id returns 204`() {
        whenever(taskService.deleteTask(1L)).thenReturn(Mono.empty())

        webTestClient.delete().uri("/api/tasks/1")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `DELETE api tasks id returns 404 when task not found`() {
        whenever(taskService.deleteTask(99L))
            .thenReturn(Mono.error(TaskNotFoundException(99L)))

        webTestClient.delete().uri("/api/tasks/99")
            .exchange()
            .expectStatus().isNotFound
    }
}
