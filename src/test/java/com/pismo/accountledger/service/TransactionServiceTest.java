package com.pismo.accountledger.service;

import com.pismo.accountledger.config.LocalStackConfig;
import com.pismo.accountledger.dto.Transaction;
import com.pismo.accountledger.exception.TransactionInProgressException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(LocalStackConfig.class)
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AccountService accountService;
    private static boolean setupDone = false;
    private static Long accountId = 0L;


    private void setupMethod() {
        if (!setupDone) {
            accountId = accountService.createAccount("withSameIdempotencyKeyAcc1")
                    .accountId();
            setupDone = true;
        }
    }
    @Test
    void createMultipleTransaction_ShouldGenerateUniqueIds() {
        var account = accountService.createAccount("MultipleTransactionTest");
        var transactions = IntStream.rangeClosed(1, 100)
                .parallel()
                .mapToObj(i -> transactionService.createTransaction(createTransaction(account.accountId()), "MultipleTransactionTest" + i))
                .toList();
        assertThat(transactions).isNotEmpty();
        assertThat(transactions.size()).isEqualTo(100);
        assertThat(transactions.stream()
                .map(Transaction::transactionId)
                .distinct()
                .count())
                .isEqualTo(100);
    }

    @Test
    void createMultipleTransaction_withSameIdempotencyKey_throwException() {
        var account = accountService.createAccount("withSameIdempotencyKeyAcc");
        Assertions.assertThrows(TransactionInProgressException.class, () -> IntStream.rangeClosed(1, 100)
                .parallel()
                .mapToObj(i -> transactionService.createTransaction(createTransaction(account.accountId()), "MultipleTransactionTest"))
                .toList());
    }

    @RepeatedTest(value = 2)
    void createMultipleTransaction_withSameIdempotencyKey_withDelayOfOneSecond() {
        setupMethod();
        assertThat(transactionService.createTransaction(createTransaction(accountId), "MultipleTransactionTest")).isNotNull();
    }

    private Transaction createTransaction(Long accountId) {
        return Transaction.builder()
                .amount(1)
                .operationTypeId(1L)
                .accountId(accountId)
                .build();
    }
}
