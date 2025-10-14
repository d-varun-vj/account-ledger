package com.pismo.accountledger.controller;

import com.pismo.accountledger.config.LocalStackConfig;
import com.pismo.accountledger.dto.Account;
import org.junit.jupiter.api.Test;
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
public class AccountControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createAccount_shouldReturnCreatedAccount() throws Exception {
        Account request = new Account(null, "12669");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Account> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Account> response = restTemplate.exchange(
                "http://localhost:" + port + "/accounts",
                HttpMethod.POST,
                entity,
                Account.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accountId()).isNotNull();
        assertThat(response.getBody().documentNumber()).isEqualTo("12669");
    }

    @Test
    void createAccount_shouldReturnConflict() throws Exception {
        Account first = new Account(null, "126691");
        Account duplicate = new Account(null, "126691");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Account> response = restTemplate.exchange(
                "http://localhost:" + port + "/accounts",
                HttpMethod.POST,
                new HttpEntity<>(first, headers),
                Account.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accountId()).isNotNull();
        assertThat(response.getBody().documentNumber()).isEqualTo("126691");

        ResponseEntity<String> responseDuplicate = restTemplate.exchange(
                "http://localhost:" + port + "/accounts",
                HttpMethod.POST,
                new HttpEntity<>(duplicate, headers),
                String.class
        );
        assertThat(responseDuplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(responseDuplicate.getBody()).contains("already exists");
    }

    @Test
    void getAccount_shouldReturnAccount() throws Exception {
        Account request = new Account(null, "126692");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Account> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Account> response = restTemplate.exchange(
                "http://localhost:" + port + "/accounts",
                HttpMethod.POST,
                entity,
                Account.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accountId()).isNotNull();
        ResponseEntity<Account> getResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/accounts/{accountId}",
                Account.class,
                response.getBody().accountId()
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().documentNumber()).isEqualTo("126692");
    }

    @Test
    void getAccount_shouldThrowNotFound() throws Exception {
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/accounts/{accountId}",
                String.class,
                "invalid"
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

}
