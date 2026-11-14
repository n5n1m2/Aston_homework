package ru.aston.exceptions;

public class ServiceTemporarilyUnavailable extends RuntimeException {
    public ServiceTemporarilyUnavailable(String message) {
        super(message);
    }
}
