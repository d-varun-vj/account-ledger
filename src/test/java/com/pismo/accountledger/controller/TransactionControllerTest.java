package com.pismo.accountledger.controller;

import com.pismo.accountledger.config.LocalStackConfig;
import com.pismo.accountledger.dto.Account;
import com.pismo.accountledger.dto.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(LocalStackConfig.class)
public class TransactionControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @ParameterizedTest
    @CsvSource({
            "1, 10101, -123",
            "2, 10102, -123",
            "3, 10103, -123",
            "4, 10104, 123"
    })
    void createTransaction_shouldReturnCreatedTransaction(Long operationTypeId, String documentNumber,
                                                          double expectedAmount) {
        var accountCreationRequest = new Account(null, documentNumber);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Account> response = restTemplate.exchange(
                "http://localhost:" + port + "/accounts",
                HttpMethod.POST,
                new HttpEntity<>(accountCreationRequest, headers),
                Account.class
        );
        assertThat(response.getBody()).isNotNull();
        var accountId = response.getBody().accountId();
        var transactionRequest = Transaction.builder()
                .amount(123)
                .operationTypeId(operationTypeId)
                .accountId(accountId)
                .build();
        ResponseEntity<Transaction> transactionResponse = restTemplate.exchange(
                "http://localhost:" + port + "/transactions",
                HttpMethod.POST,
                new HttpEntity<>(transactionRequest, headers),
                Transaction.class
        );
        assertThat(transactionResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(transactionResponse.getBody()).isNotNull();
        assertThat(transactionResponse.getBody().transactionId()).isNotNull();
        assertThat(transactionResponse.getBody().accountId()).isEqualTo(accountId);
        assertThat(transactionResponse.getBody().amount()).isEqualTo(expectedAmount);
    }

    @Test
    void createTransaction_invalidAccount_throwsException() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var transactionRequest = Transaction.builder()
                .amount(123)
                .operationTypeId(1L)
                .accountId(1010L)
                .build();
        ResponseEntity<String> transactionResponse = restTemplate.exchange(
                "http://localhost:" + port + "/transactions",
                HttpMethod.POST,
                new HttpEntity<>(transactionRequest, headers),
                String.class
        );
        assertThat(transactionResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(transactionResponse.getBody()).contains("Invalid Account");
    }

    @Test
    void createTransaction_invalidAmount_throwsException() {
        var accountCreationRequest = new Account(null, "documentNumber1");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Account> response = restTemplate.exchange(
                "http://localhost:" + port + "/accounts",
                HttpMethod.POST,
                new HttpEntity<>(accountCreationRequest, headers),
                Account.class
        );
        assertThat(response.getBody()).isNotNull();
        var accountId = response.getBody().accountId();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var transactionRequest = Transaction.builder()
                .amount(0)
                .operationTypeId(1L)
                .accountId(accountId)
                .build();
        ResponseEntity<String> transactionResponse = restTemplate.exchange(
                "http://localhost:" + port + "/transactions",
                HttpMethod.POST,
                new HttpEntity<>(transactionRequest, headers),
                String.class
        );
        assertThat(transactionResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(transactionResponse.getBody()).contains("Amount cannot be zero");
    }

}
