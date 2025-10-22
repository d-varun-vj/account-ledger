package com.pismo.accountledger.service.impl;

import com.pismo.accountledger.dto.OperationType;
import com.pismo.accountledger.dto.Transaction;
import com.pismo.accountledger.dto.enums.TypeEnum;
import com.pismo.accountledger.exception.InvalidTransactionException;
import com.pismo.accountledger.exception.TransactionInProgressException;
import com.pismo.accountledger.repository.AccountRepository;
import com.pismo.accountledger.repository.CounterRepository;
import com.pismo.accountledger.repository.IdempotencyRepository;
import com.pismo.accountledger.repository.OperationTypeRepository;
import com.pismo.accountledger.repository.TransactionRepository;
import com.pismo.accountledger.service.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

import static com.pismo.accountledger.util.Constants.TRANSACTION_PREFIX;

@Service
@AllArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final OperationTypeRepository operationTypeRepository;
    private final CounterRepository counterRepository;
    private final IdempotencyRepository idempotencyRepository;

    @Override
    public Transaction createTransaction(Transaction transaction, String idempotencyKey) {
        var operationType = operationTypeRepository.getOperationById(transaction.operationTypeId())
                .orElseThrow(() -> new InvalidTransactionException("Invalid operation type id: " + transaction.operationTypeId()));
        if (!accountRepository.existsById(transaction.accountId().toString())) {
            throw new InvalidTransactionException("Invalid Account: " + transaction.accountId());
        }
        return idempotencyRepository.getTransactionId(idempotencyKey)
                .map(txnId -> constructExistingTransaction(transaction, txnId))
                .orElseGet(() -> saveTransaction(transaction, operationType, idempotencyKey));
    }

    private Transaction constructExistingTransaction(Transaction transaction, String txnId) {
        var txnLongValue = Long.valueOf(txnId.substring(TRANSACTION_PREFIX.length()));
        log.info("Transaction already persisted, transactionId {}", txnLongValue);
        return transactionRepository.findByTransactionId(transaction.accountId(), txnId)
                .orElseThrow(TransactionInProgressException::new);
    }

    private Transaction saveTransaction(Transaction transaction, OperationType opType, String idempotencyKey) {
        var transactionAmount = opType.isCredit() ? transaction.amount() : -transaction.amount();
        var transactionId = counterRepository.getNextId(TypeEnum.TRANSACTION.name());
        try {
            transactionRepository.saveIdempotentTransaction(transaction, transactionAmount, transactionId, idempotencyKey);
            log.info("Transaction created successfully, transactionId {}", transactionId);
            return Transaction.builder()
                    .transactionId(transactionId)
                    .amount(transactionAmount)
                    .accountId(transaction.accountId())
                    .operationTypeId(transaction.operationTypeId())
                    .build();
        } catch (TransactionCanceledException ex) {
            log.info("Transaction request already in progress, idempotencyKey {}, transactionId {}", idempotencyKey, transactionId);
            throw new TransactionInProgressException();
        }
    }
}
