package com.pismo.accountledger.service;

import com.pismo.accountledger.dto.Transaction;

public interface TransactionService {
    Transaction createTransaction(Transaction transaction, String idempotencyKey);
}
