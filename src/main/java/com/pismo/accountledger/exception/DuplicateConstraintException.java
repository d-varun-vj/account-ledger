package com.pismo.accountledger.exception;

public class DuplicateConstraintException extends RuntimeException {
    private final String field;

    public DuplicateConstraintException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
