package com.pismo.accountledger.exception;

public class DynamoDBTransactionException extends RuntimeException {
    public DynamoDBTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
