package abopijservice.code.issuecatalog.exception

class TaskNotFoundException(id: Long) : RuntimeException("Task not found: $id")
