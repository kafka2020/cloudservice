package ru.netology.cloudservice.dto;

import jakarta.validation.constraints.NotBlank;

public record RenameRequest(@NotBlank String name) { }
