package ru.aston.exceptions;

public class StorageUnavailableException extends RuntimeException {
    public StorageUnavailableException(String message) {
        super(message);
    }
}
