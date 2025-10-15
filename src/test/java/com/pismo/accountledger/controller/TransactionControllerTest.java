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
            "1, 10101, -123, parameterizedTest1",
            "2, 10102, -123, parameterizedTest2",
            "3, 10103, -123, parameterizedTest3",
            "4, 10104, 123, parameterizedTest4"
    })
    void createTransaction_shouldReturnCreatedTransaction(Long operationTypeId, String documentNumber,
                                                          double expectedAmount, String idempotencyKey) {
        var accountCreationRequest = new Account(null, documentNumber);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Idempotency-Key", idempotencyKey);
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
        headers.add("Idempotency-Key", "invalidAccount");
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
    void createTransaction_withoutAccountId_throwValidationException() {
        var accountCreationRequest = new Account(null, "invalidAccountId");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Idempotency-Key", "withoutAccountId");
        ResponseEntity<Account> response = restTemplate.exchange(
                "http://localhost:" + port + "/accounts",
                HttpMethod.POST,
                new HttpEntity<>(accountCreationRequest, headers),
                Account.class
        );
        assertThat(response.getBody()).isNotNull();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var transactionRequest = Transaction.builder()
                .amount(0)
                .operationTypeId(1L)
                .build();
        ResponseEntity<String> transactionResponse = restTemplate.exchange(
                "http://localhost:" + port + "/transactions",
                HttpMethod.POST,
                new HttpEntity<>(transactionRequest, headers),
                String.class
        );
        assertThat(transactionResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(transactionResponse.getBody()).contains("Invalid accountId");
    }

    @Test
    void createTransaction_withoutAmount_throwValidationException() {
        var accountCreationRequest = new Account(null, "invalidAmount");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Idempotency-Key", "withoutAmount");
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
                .amount(0.0)
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
        assertThat(transactionResponse.getBody()).contains("Amount must be greater than zero");
    }

    @Test
    void createTransaction_withoutOperationTypeId_throwValidationException() {
        var accountCreationRequest = new Account(null, "invalidOperationId");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Idempotency-Key", "withoutOperationTypeId");
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
                .amount(100)
                .accountId(accountId)
                .build();
        ResponseEntity<String> transactionResponse = restTemplate.exchange(
                "http://localhost:" + port + "/transactions",
                HttpMethod.POST,
                new HttpEntity<>(transactionRequest, headers),
                String.class
        );
        assertThat(transactionResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(transactionResponse.getBody()).contains("Invalid operation type ID");
    }

    @Test
    void createTransaction_withoutIdempotencyHeader_throwException() {
        var accountCreationRequest = new Account(null, "withoutIdempotencyHeader");
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
                .amount(100.0)
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
        assertThat(transactionResponse.getBody()).contains("Required header 'Idempotency-Key' is not present.");
    }

}
