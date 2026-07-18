package ru.netology.cloudservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Тело запроса переименования.
 * Спецификация YAML описывает поле "name", но реальный фронт Нетологии
 * (src/views/Home.vue) присылает поле "filename". Поддерживаем оба имени.
 */
public record RenameRequest(
        @JsonAlias({"name", "filename"}) String filename
) { }
