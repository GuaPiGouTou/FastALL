package org.example.ecmo.exception;

public class FileStorageException extends BusinessException {
    
    public FileStorageException(String message) {
        super(message);
    }
    
    public FileStorageException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
