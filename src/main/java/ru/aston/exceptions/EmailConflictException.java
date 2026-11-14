package ru.aston.exceptions;

public class EmailConflictException extends RuntimeException {
    public EmailConflictException(String message) {
        super(message);
    }
}
