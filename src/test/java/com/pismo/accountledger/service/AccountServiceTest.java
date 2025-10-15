package com.pismo.accountledger.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.pismo.accountledger.config.LocalStackConfig;
import com.pismo.accountledger.dto.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.IntStream;

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
}
