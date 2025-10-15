package com.pismo.accountledger.service.impl;

import com.pismo.accountledger.dto.OperationType;
import com.pismo.accountledger.dto.Transaction;
import com.pismo.accountledger.dto.enums.TypeEnum;
import com.pismo.accountledger.exception.InvalidTransactionException;
import com.pismo.accountledger.repository.AccountRepository;
import com.pismo.accountledger.repository.CounterRepository;
import com.pismo.accountledger.repository.OperationTypeRepository;
import com.pismo.accountledger.repository.TransactionRepository;
import com.pismo.accountledger.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final OperationTypeRepository operationTypeRepository;
    private final CounterRepository counterRepository;

    @Override
    public Transaction createTransaction(Transaction transaction) {
        var operationType = operationTypeRepository.getOperationById(transaction.operationTypeId());
        validateTransactionRequestPayload(transaction, operationType.isEmpty());
        var transactionBuilder = Transaction.builder();
        operationType.ifPresent(opType -> saveTransaction(transaction, opType, transactionBuilder));
        return transactionBuilder.build();
    }

    private void validateTransactionRequestPayload(Transaction transaction, boolean isOperationTypeEmpty) {
        if (!accountRepository.existsById(transaction.accountId().toString())) {
            throw new InvalidTransactionException("Invalid Account: " + transaction.accountId());
        }
        if (isOperationTypeEmpty) {
            throw new InvalidTransactionException("Invalid operation type id: " + transaction.operationTypeId());
        }
    }

    private void saveTransaction(Transaction transaction, OperationType opType, Transaction.TransactionBuilder transactionBuilder) {
        var transactionAmount = opType.isCredit() ? transaction.amount() : -transaction.amount();
        var transactionId = counterRepository.getNextId(TypeEnum.TRANSACTION.name());
        transactionRepository.createTransaction(transaction, transactionAmount, transactionId);
        transactionBuilder.transactionId(transactionId)
                .amount(transaction.amount())
                .accountId(transaction.accountId())
                .operationTypeId(transaction.operationTypeId());
    }
}
