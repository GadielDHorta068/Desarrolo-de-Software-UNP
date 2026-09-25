package com.desarrollo.raffy.exception;

public class ReviewAlreadyExistsException extends RuntimeException {
    public ReviewAlreadyExistsException(String msg) {
        super(msg); 
    }
}
