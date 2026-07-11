package ru.netology.cloudservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final int id;

    public ApiException(HttpStatus status, int id, String message) {
        super(message);
        this.status = status;
        this.id = id;
    }
}
