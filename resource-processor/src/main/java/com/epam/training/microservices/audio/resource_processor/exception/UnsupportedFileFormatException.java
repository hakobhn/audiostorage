package com.epam.training.microservices.audio.resource_processor.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UnsupportedFileFormatException extends RuntimeException {

    public UnsupportedFileFormatException(String message) {
        super(message);
    }

    public UnsupportedFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }

}