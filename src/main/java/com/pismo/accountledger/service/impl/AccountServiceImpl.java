package com.pismo.accountledger.service.impl;

import com.pismo.accountledger.exception.AccountNotFoundException;
import com.pismo.accountledger.exception.DuplicateConstraintException;
import com.pismo.accountledger.exception.DynamoDBTransactionException;
import com.pismo.accountledger.dto.Account;
import com.pismo.accountledger.repository.AccountRepository;
import com.pismo.accountledger.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.CancellationReason;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final String ACCOUNT_PREFIX = "ACCOUNT#";
    private final DynamoDbClient dynamoDbClient;
    private final AccountRepository accountRepository;
    //TODO move to properties
    private static final String TABLE_NAME = "account_ledger";
    //Consider scalable solution
    private static final AtomicInteger COUNTER = new AtomicInteger(1);


    @Override
    public Account createAccount(String documentNumber) {
        try {
            var accountId = accountRepository.createAccount(documentNumber);
            return Account.builder()
                    .accountId(accountId)
                    .documentNumber(documentNumber)
                    .build();
        } catch (TransactionCanceledException ex) {
            handleTransactionException(ex, documentNumber);
        }
        return Account.builder().build();
    }

    @Override
    public Account getAccount(String accountId) {
        return accountRepository.getAccount(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private void handleTransactionException(TransactionCanceledException e, String documentNumber) {
        var reasons = e.cancellationReasons();

        if (reasons != null) {
            for (int i = 0; i < reasons.size(); i++) {
                CancellationReason reason = reasons.get(i);
                if ("ConditionalCheckFailed".equals(reason.code())) {
                    // Only first write (index 0) has condition
                    if (i == 0) {
                        throw new DuplicateConstraintException("documentNumber", "Document number already exists");
                    }
                }
            }
        }
        throw new DynamoDBTransactionException("Transaction failed: " + e.getMessage(), e);
    }
}
