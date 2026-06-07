package ru.yandex.practicum.exceptions.handler;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class ErrorResponse {
    HttpStatus status;
    ErrorCodes error;
    String message;
    String userMessage;
    String path;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp;
    List<String> validationErrors;
}
