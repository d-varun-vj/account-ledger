package com.pismo.accountledger.service.impl;

import com.pismo.accountledger.dto.Transaction;
import com.pismo.accountledger.repository.TransactionRepository;
import com.pismo.accountledger.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;


    @Override
    public Transaction createTransaction(Transaction transaction) {
        validateTransactionPayload(transaction);
        var transactionId = transactionRepository.createTransaction(transaction);
        return Transaction.builder()
                .transactionId(transactionId)
                .accountId(transaction.accountId())
                .operationTypeId(transaction.operationTypeId())
                .amount(transaction.amount())
                .build();
    }

    private void validateTransactionPayload(Transaction transaction) {

    }
}
