package com.pismo.accountledger.service.impl;

import com.pismo.accountledger.dto.Account;
import com.pismo.accountledger.dto.enums.TypeEnum;
import com.pismo.accountledger.exception.AccountNotFoundException;
import com.pismo.accountledger.exception.DuplicateConstraintException;
import com.pismo.accountledger.exception.DynamoDBTransactionException;
import com.pismo.accountledger.repository.AccountRepository;
import com.pismo.accountledger.repository.CounterRepository;
import com.pismo.accountledger.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.CancellationReason;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

@Slf4j
@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CounterRepository counterRepository;

    @Override
    public Account createAccount(String documentNumber) {
        try {
            // check constraint table has documentNumber
            // yes - throw error account already exist
            // no - create new id and create account
            var accountId = counterRepository.getNextId(TypeEnum.ACCOUNT.name());
            accountRepository.createAccount(documentNumber, String.valueOf(accountId));
            return Account.builder()
                    .accountId(accountId)
                    .documentNumber(documentNumber)
                    .build();
        } catch (TransactionCanceledException ex) {
            log.error("Account creation failed, {}", ex.getMessage());
            handleTransactionException(ex);
        }
        return Account.builder().build();
    }

    @Override
    public Account getAccount(Long accountId) {
        return accountRepository.getAccount(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private void handleTransactionException(TransactionCanceledException e) {
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
