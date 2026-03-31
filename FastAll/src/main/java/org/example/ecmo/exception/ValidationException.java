package org.example.ecmo.exception;

public class ValidationException extends BusinessException {
    
    public ValidationException(String message) {
        super(400, message);
    }
}
