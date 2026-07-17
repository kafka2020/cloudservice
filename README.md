# Cloud Service — Дипломная работа Нетологии

REST-сервис «Облачное хранилище». Позволяет авторизованному пользователю
загружать, скачивать, переименовывать, удалять файлы и получать список
своих файлов. Спецификация — `CloudServiceSpecification.yaml` из задания.

## Стек
- Java 17, Spring Boot 3.2
- Spring Web, Spring Security, Spring Data JPA, Bean Validation
- JWT (jjwt 0.12)
- PostgreSQL 16 + Liquibase
- Maven
- JUnit 5, Mockito, Testcontainers
- Docker / docker-compose

## Запуск через docker-compose
```bash
docker compose up --build
```
Backend будет доступен на `http://localhost:8080`, Postgres — на `5432`.

В миграциях создаётся демо-пользователь:
- логин: `user1`
- пароль: `password`

## Запуск локально (для разработки)
1. Поднимите Postgres:
   ```bash
   docker compose up db
   ```
2. Соберите и запустите:
   ```bash
   ./mvnw spring-boot:run
   # или
   mvn spring-boot:run
   ```

## Тесты
```bash
mvn test
```
- Unit-тесты используют Mockito (`FileServiceTest`, `AuthServiceTest`).
- Интеграционный тест поднимает Postgres через Testcontainers
  (`CloudServiceIntegrationTest`), требуется работающий Docker.

## Подключение FRONT
1. Скачайте FRONT (см. ссылку в задании).
2. В файле `.env` фронта установите:
   ```
   VUE_APP_BASE_URL=http://localhost:8080
   ```
3. Запустите фронт: `npm install && npm run serve`. Он поднимется на 8081.
4. CORS на бекенде уже настроен на `http://localhost:8081` (переопределяется
   переменной `ALLOWED_ORIGINS`).

## Авторизация
- `POST /login` принимает JSON `{login, password}`, возвращает
  `{"auth-token": "..."}`.
- Все остальные эндпоинты требуют заголовок `auth-token: <token>`.
- `POST /logout` отзывает токен (попадает в blacklist).

## Эндпоинты
| Метод | URL | Описание |
|------|-----|---------|
| POST | `/login` | Логин, выдача токена |
| POST | `/logout` | Логаут |
| POST | `/file?filename=...` | Загрузка файла (multipart `file`) |
| DELETE | `/file?filename=...` | Удаление файла |
| GET | `/file?filename=...` | Скачивание файла |
| PUT | `/file?filename=...` | Переименование (`{"name": "new"}`) |
| GET | `/list?limit=N` | Список файлов пользователя |

## Примеры запросов
```bash
# Логин
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"login":"user1","password":"password"}'

# Загрузка
curl -X POST "http://localhost:8080/file?filename=hello.txt" \
  -H "auth-token: <token>" \
  -F "file=@hello.txt"

# Список
curl http://localhost:8080/list -H "auth-token: <token>"
```
