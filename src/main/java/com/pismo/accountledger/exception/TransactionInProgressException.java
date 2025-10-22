package com.pismo.accountledger.exception;

public class TransactionInProgressException extends RuntimeException {

    public TransactionInProgressException() {
        super("A transaction is already in progress for this request.");
    }
}
