package com.pismo.accountledger.service.impl;

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
        validateTransactionPayload(transaction);
        var operationType = operationTypeRepository.getOperationById(transaction.operationTypeId());
        if (operationType.isEmpty()) {
            throw new InvalidTransactionException("Invalid operation type id: " + transaction.operationTypeId());
        }
        var transactionAmount = operationType.get().isCredit() ? transaction.amount() : -transaction.amount();
        var transactionId = counterRepository.getNextId(TypeEnum.TRANSACTION.name());
        transactionRepository.createTransaction(transaction, transactionAmount, transactionId);
        return Transaction.builder()
                .transactionId(transactionId)
                .accountId(transaction.accountId())
                .operationTypeId(transaction.operationTypeId())
                .amount(transactionAmount)
                .build();
    }

    private void validateTransactionPayload(Transaction transaction) {
        if (!accountRepository.existsById(transaction.accountId().toString())) {
            throw new InvalidTransactionException("Invalid Account: " + transaction.accountId());
        }

        // 3. Validate amount
        if (transaction.amount() == 0.0) {
            throw new InvalidTransactionException("Amount cannot be zero");
        }

    }
}
