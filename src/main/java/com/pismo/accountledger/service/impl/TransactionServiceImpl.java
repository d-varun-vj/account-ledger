package com.pismo.accountledger.service.impl;

import com.pismo.accountledger.dto.OperationType;
import com.pismo.accountledger.dto.Transaction;
import com.pismo.accountledger.dto.enums.TypeEnum;
import com.pismo.accountledger.exception.InvalidTransactionException;
import com.pismo.accountledger.repository.AccountRepository;
import com.pismo.accountledger.repository.CounterRepository;
import com.pismo.accountledger.repository.OperationTypeRepository;
import com.pismo.accountledger.repository.TransactionIdempotencyRepository;
import com.pismo.accountledger.repository.TransactionRepository;
import com.pismo.accountledger.service.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final OperationTypeRepository operationTypeRepository;
    private final CounterRepository counterRepository;
    private final TransactionIdempotencyRepository idempotencyRepository;

    @Override
    public Transaction createTransaction(Transaction transaction, String idempotencyKey) {
        var operationType = operationTypeRepository.getOperationById(transaction.operationTypeId());
        validateTransactionRequestPayload(transaction, operationType.isEmpty());
        Optional<String> existingTransactionId = idempotencyRepository.getTransactionId(idempotencyKey);
        if (existingTransactionId.isPresent()) {
            log.info("Transaction was already persisted, transactionId {}", existingTransactionId.get());
            return transactionRepository.findByTransactionId(transaction.accountId(), existingTransactionId.get())
                    .orElseThrow(() -> new RuntimeException("Ledger transaction missing"));
        }
        var transactionBuilder = Transaction.builder();
        operationType.ifPresent(opType -> saveTransaction(transaction, opType, transactionBuilder, idempotencyKey));
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

    private void saveTransaction(Transaction transaction, OperationType opType,
                                 Transaction.TransactionBuilder transactionBuilder, String idempotencyKey) {
        var transactionAmount = opType.isCredit() ? transaction.amount() : -transaction.amount();
        var transactionId = counterRepository.getNextId(TypeEnum.TRANSACTION.name());
        Transaction transactionToUse;
        try {
            transactionRepository.saveIdempotentTransaction(transaction, transactionAmount, transactionId, idempotencyKey);
            transactionToUse = Transaction.builder()
                    .transactionId(transactionId)
                    .amount(transactionAmount)
                    .accountId(transaction.accountId())
                    .operationTypeId(transaction.operationTypeId())
                    .build();
            log.info("Transaction created successfully, transactionId {}", transactionToUse.transactionId());
        } catch (TransactionCanceledException ex) {
            Optional<String> existingTransactionId = idempotencyRepository.getTransactionId(idempotencyKey);
            transactionToUse = existingTransactionId
                    .flatMap(txId -> transactionRepository.findByTransactionId(transaction.accountId(), txId))
                    .orElseThrow(() -> new RuntimeException("Ledger transaction missing"));
            log.info("Transaction was already persisted, transactionId {}", transactionToUse.transactionId());
        }
        transactionBuilder.transactionId(transactionToUse.transactionId())
                .amount(transactionToUse.amount())
                .accountId(transactionToUse.accountId())
                .operationTypeId(transactionToUse.operationTypeId());
    }
}
