package com.pismo.accountledger.service;

import com.pismo.accountledger.config.LocalStackConfig;
import com.pismo.accountledger.dto.Account;
import com.pismo.accountledger.exception.DuplicateConstraintException;
import org.junit.jupiter.api.Assertions;
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
public class AccountServiceTest {
    @Autowired
    private AccountService accountService;

    @Test
    void createMultipleAccounts_ShouldGenerateUniqueIds() {
        var accounts = IntStream.rangeClosed(1, 100)
                .parallel()
                .mapToObj(i -> accountService.createAccount("DOC" + i))
                .toList();
        assertThat(accounts).isNotEmpty();
        assertThat(accounts.size()).isEqualTo(100);
        assertThat(accounts.stream()
                .map(Account::accountId)
                .distinct()
                .count())
                .isEqualTo(100);
    }

    @Test
    void createAccounts_saveSameAccountAgain_ShouldThrowException() {
        Assertions.assertThrows(DuplicateConstraintException.class, () -> IntStream.rangeClosed(1, 2)
                .parallel()
                .mapToObj(i -> accountService.createAccount("DOC"))
                .toList());
    }
}
