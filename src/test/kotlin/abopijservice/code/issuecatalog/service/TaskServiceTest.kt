package abopijservice.code.issuecatalog.service

import abopijservice.code.issuecatalog.exception.TaskNotFoundException
import abopijservice.code.issuecatalog.model.Task
import abopijservice.code.issuecatalog.model.TaskStatus
import abopijservice.code.issuecatalog.repository.TaskRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import reactor.test.StepVerifier
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class TaskServiceTest {

    @Mock
    private lateinit var repository: TaskRepository

    @InjectMocks
    private lateinit var service: TaskService

    private val now = LocalDateTime.now()
    private val task = Task(1L, "Test task", "description", TaskStatus.NEW, now, now)

    @Test
    fun `createTask returns task response`() {
        whenever(repository.save("Test task", "description")).thenReturn(task)

        StepVerifier.create(service.createTask("Test task", "description"))
            .assertNext { response ->
                assert(response.id == 1L)
                assert(response.title == "Test task")
                assert(response.status == TaskStatus.NEW)
            }
            .verifyComplete()
    }

    @Test
    fun `getTaskById returns task when found`() {
        whenever(repository.findById(1L)).thenReturn(task)

        StepVerifier.create(service.getTaskById(1L))
            .assertNext { assert(it.id == 1L) }
            .verifyComplete()
    }

    @Test
    fun `getTaskById throws TaskNotFoundException when not found`() {
        whenever(repository.findById(99L)).thenReturn(null)

        StepVerifier.create(service.getTaskById(99L))
            .expectError(TaskNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `updateStatus returns updated task`() {
        val updated = task.copy(status = TaskStatus.DONE, updatedAt = now.plusMinutes(1))
        whenever(repository.updateStatus(1L, TaskStatus.DONE)).thenReturn(updated)

        StepVerifier.create(service.updateStatus(1L, TaskStatus.DONE))
            .assertNext { assert(it.status == TaskStatus.DONE) }
            .verifyComplete()
    }

    @Test
    fun `updateStatus throws TaskNotFoundException when task not found`() {
        whenever(repository.updateStatus(99L, TaskStatus.DONE)).thenReturn(null)

        StepVerifier.create(service.updateStatus(99L, TaskStatus.DONE))
            .expectError(TaskNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `deleteTask completes when task exists`() {
        whenever(repository.deleteById(1L)).thenReturn(true)

        StepVerifier.create(service.deleteTask(1L))
            .verifyComplete()
    }

    @Test
    fun `deleteTask throws TaskNotFoundException when not found`() {
        whenever(repository.deleteById(99L)).thenReturn(false)

        StepVerifier.create(service.deleteTask(99L))
            .expectError(TaskNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `getTasks returns paginated response filtered by status`() {
        whenever(repository.findAll(0, 10, TaskStatus.NEW)).thenReturn(listOf(task))
        whenever(repository.count(TaskStatus.NEW)).thenReturn(1L)

        StepVerifier.create(service.getTasks(0, 10, TaskStatus.NEW))
            .assertNext { page ->
                assert(page.content.size == 1)
                assert(page.totalElements == 1L)
                assert(page.totalPages == 1)
                assert(page.page == 0)
                assert(page.size == 10)
            }
            .verifyComplete()
    }

    @Test
    fun `getTasks returns all tasks without status filter`() {
        whenever(repository.findAll(0, 5, null)).thenReturn(listOf(task, task))
        whenever(repository.count(null)).thenReturn(2L)

        StepVerifier.create(service.getTasks(0, 5, null))
            .assertNext { page ->
                assert(page.content.size == 2)
                assert(page.totalElements == 2L)
                assert(page.totalPages == 1)
            }
            .verifyComplete()
    }

    @Test
    fun `getTasks calculates totalPages correctly for multiple pages`() {
        val tasks = List(5) { task }
        whenever(repository.findAll(0, 5, null)).thenReturn(tasks)
        whenever(repository.count(null)).thenReturn(11L)

        StepVerifier.create(service.getTasks(0, 5, null))
            .assertNext { page ->
                assert(page.totalPages == 3)
            }
            .verifyComplete()
    }
}
