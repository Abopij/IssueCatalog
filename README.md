# IssueCatalog

Реактивное REST API для управления задачами (issues), построенное на **Spring Boot 3** + **Kotlin** + **WebFlux**.

## Стек технологий

| Слой | Технология |
|------|-----------|
| Язык | Kotlin 1.9 |
| Фреймворк | Spring Boot 3.4.4 |
| Веб | Spring WebFlux (реактивный) |
| Корутины | kotlinx-coroutines-reactor |
| База данных | H2 in-memory (dev/test) |
| Миграции | Flyway |
| Сборка | Maven |

## Структура проекта

```
src/main/kotlin/.../issuecatalog/
├── controller/     # REST-контроллеры
├── service/        # Бизнес-логика
├── repository/     # Работа с БД (JDBC)
├── model/          # Доменные модели (Task, TaskStatus)
├── dto/            # DTO запросов и ответов
└── exception/      # Глобальная обработка ошибок

src/main/resources/
├── application.properties
└── db/migration/   # Flyway-скрипты
```

## Модель Task

| Поле | Тип | Описание |
|------|-----|----------|
| `id` | Long | Автоинкремент |
| `title` | String | Заголовок (3–100 символов, обязательный) |
| `description` | String? | Описание (опционально) |
| `status` | TaskStatus | Текущий статус |
| `createdAt` | LocalDateTime | Дата создания |
| `updatedAt` | LocalDateTime | Дата последнего изменения |

### Статусы задачи

```
NEW → IN_PROGRESS → DONE
                 ↘ CANCELLED
```

## API

Базовый путь: `/api/tasks`

### Создать задачу
```http
POST /api/tasks
Content-Type: application/json

{
  "title": "Fix login bug",
  "description": "Users can't log in with OAuth"
}
```
**Ответ:** `201 Created` — объект `TaskResponse`

---

### Получить список задач (с пагинацией)
```http
GET /api/tasks?page=0&size=10&status=NEW
```
| Параметр | Обязательный | Описание |
|----------|-------------|----------|
| `page` | да | Номер страницы (от 0) |
| `size` | да | Размер страницы |
| `status` | нет | Фильтр по статусу: `NEW`, `IN_PROGRESS`, `DONE`, `CANCELLED` |

**Ответ:** `200 OK` — `PageResponse<TaskResponse>`

---

### Получить задачу по ID
```http
GET /api/tasks/{id}
```
**Ответ:** `200 OK` — объект `TaskResponse`, либо `404 Not Found`

---

### Обновить статус задачи
```http
PATCH /api/tasks/{id}/status
Content-Type: application/json

{
  "status": "IN_PROGRESS"
}
```
**Ответ:** `200 OK` — обновлённый `TaskResponse`

---

### Удалить задачу
```http
DELETE /api/tasks/{id}
```
**Ответ:** `204 No Content`

## Запуск

### Требования

- Java 21+
- Maven 3.9+ (или используйте `./mvnw`)

### Локальный запуск (H2 in-memory)

По умолчанию приложение использует встроенную H2-базу — дополнительная настройка не нужна.

```bash
./mvnw spring-boot:run
```

Приложение запустится на `http://localhost:8080`.

### С PostgreSQL

Переопределите параметры в `application.properties` или через переменные окружения:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/issuecatalog
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=your_user
spring.datasource.password=your_password
```

Flyway применит миграции автоматически при старте.

## Тесты

```bash
./mvnw test
```

Покрыты:
- **Unit-тесты** сервисного слоя (`TaskServiceTest`)
- **Интеграционные тесты** контроллера через `WebTestClient` (`TaskControllerTest`)
