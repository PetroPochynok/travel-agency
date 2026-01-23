package com.epam.finaltask.exception;

import java.time.LocalDateTime;

public record ApiError(String message, LocalDateTime timestamp) {
}