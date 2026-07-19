# Cloud Service — Дипломная работа Нетологии

REST-сервис «Облачное хранилище». Авторизованный пользователь может загружать,
скачивать, переименовывать, удалять файлы и получать список своих файлов.
Контракт API описан в `CloudServiceSpecification.yaml` из задания.

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
3. Запустите фронт: `npm install && npm run serve`. Он поднимется на 8081
   (потому что 8080 уже занят бекендом).
4. CORS на бекенде уже настроен на `http://localhost:8081` (переопределяется
   переменной `ALLOWED_ORIGINS`).

## Авторизация
- `POST /login` принимает JSON `{login, password}`, возвращает
  `{"auth-token": "..."}` (имя поля — с дефисом).
- Все остальные эндпоинты требуют заголовок `auth-token: <token>`.
- Реальный фронт Нетологии шлёт заголовок в формате `auth-token: Bearer <jwt>` —
  бекенд это поддерживает: префикс `Bearer ` срезается на стороне фильтра.
- `POST /logout` отзывает токен (попадает в blacklist).

## Эндпоинты
| Метод | URL | Описание |
|------|-----|---------|
| POST | `/login` | Логин, выдача токена |
| POST | `/logout` | Логаут |
| POST | `/file?filename=...` | Загрузка файла (multipart-поле `file`) |
| DELETE | `/file?filename=...` | Удаление файла |
| GET | `/file?filename=...` | Скачивание файла |
| PUT | `/file?filename=...` | Переименование (`{"filename": "new"}`) |
| GET | `/list?limit=N` | Список файлов пользователя |

## Расхождения со спецификацией и почему так
- В YAML тело PUT `/file` описано как `{ "name": "..." }`, но реальный фронт
  (`src/views/Home.vue`) шлёт `{ "filename": "..." }`. Поддерживаем оба варианта
  через `@JsonAlias({"name", "filename"})` в `RenameRequest`.
- В YAML стоит `servers: http://localhost:8080/cloud`, но в README задания и в
  `.env` фронта используется корневой URL без префикса. Соответственно в
  `application.yml` нет `server.servlet.context-path`.

## Примеры запросов
```bash
# Логин
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"login":"user1","password":"password"}'

# Загрузка
curl -X POST "http://localhost:8080/file?filename=hello.txt" \
  -H "auth-token: Bearer <token>" \
  -F "file=@hello.txt"

# Список
curl http://localhost:8080/list -H "auth-token: Bearer <token>"

# Переименование
curl -X PUT "http://localhost:8080/file?filename=hello.txt" \
  -H "auth-token: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"filename":"hi.txt"}'
```
