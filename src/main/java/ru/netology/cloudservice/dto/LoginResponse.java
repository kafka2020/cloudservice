package ru.netology.cloudservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ВНИМАНИЕ: фронт ждёт именно поле "auth-token" (с дефисом),
 * поэтому используем @JsonProperty.
 */
public record LoginResponse(
        @JsonProperty("auth-token") String authToken
) { }
