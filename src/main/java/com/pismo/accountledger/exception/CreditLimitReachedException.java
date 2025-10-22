package com.pismo.accountledger.exception;

public class CreditLimitReachedException extends  RuntimeException {

    public CreditLimitReachedException(String message) {
        super(message);
    }
}
