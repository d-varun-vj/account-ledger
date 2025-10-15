package com.pismo.accountledger.controller;

import com.pismo.accountledger.dto.Account;
import com.pismo.accountledger.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
@Validated
@Tag(name = "Account API", description = "Operations related to customer accounts")
public class AccountController {

    @Autowired
    private final AccountService accountService;

    @Operation(
            summary = "Create a new account",
            description = "Creates a customer account with a unique document number",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Account created successfully",
                            content = @Content(schema = @Schema(implementation = Account.class))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Document number already exists",
                            content = @Content(schema = @Schema())
                    )
            }
    )
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Account> saveAccount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Account creation request containing the document number",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Account.class)
                    )
            )
            @Valid @RequestBody Account account) {
        var accountPersisted = accountService.createAccount(account.documentNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(accountPersisted);
    }

    @Operation(
            summary = "Get an account by ID",
            description = "Retrieves account details by account ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Account retrieved successfully",
                            content = @Content(schema = @Schema(implementation = Account.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Account not found",
                            content = @Content(schema = @Schema())
                    )
            }
    )
    @GetMapping(value = "/{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Account> fetchAccount(
            @Parameter(
                    description = "Unique identifier of the account",
                    example = "1",
                    required = true
            )
            @PathVariable(value = "accountId") Long accountId) {
        var account = accountService.getAccount(accountId);
        return ResponseEntity.ok(account);
    }
}
