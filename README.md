# Graduation

Это приложение для поиска, создания и участия в мероприятиях

Имеет микросервисную архитектуру. Для передачи данных используется REST API, gRPC, а также Kafka (по схемам Avro).

---

## Используемые Технологии

| Технология     | Версия   |
|:---------------|:---------|
| Spring Boot    | 3.3.2    |
| Spring Cloud   | 2023.0.3 |
| Kafka (client) | 3.6.1    |
| PostgreSQL     | 16.1     |
| gRPC           | 1.63.0   |
| Docker         | 4.36.0   |

---

## Спецификация

* **[Основная](specifications/main-specification.json)** - Общая спецификация основных сервисов
* **[Комментарии](specifications/comments-specification.md)** - Спецификация дополнительной функциональности комментарии

---

## Быстрый старт в Docker

* Запуск: `docker compose up -d`
* Остановка: `docker compose stop`
* Перезапуск сервиса: `docker compose restart <service_name>` (например, event-service)