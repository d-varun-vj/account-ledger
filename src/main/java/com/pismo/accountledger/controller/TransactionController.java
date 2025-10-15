package com.pismo.accountledger.controller;

import com.pismo.accountledger.dto.Transaction;
import com.pismo.accountledger.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Transaction API", description = "Operations related to customer transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(
            summary = "Create new transaction for a customer account",
            description = """
                    Creates a new customer transaction for an existing account.
                    The operation type determines whether the transaction is debit or credit.
                    Example operation types:
                    - 1: PURCHASE
                    - 2: INSTALLMENT PURCHASE
                    - 3: WITHDRAWAL
                    - 4: PAYMENT (is a credit transaction, rest all are debit)
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Transaction created successfully",
                            content = @Content(schema = @Schema(implementation = Transaction.class))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Document number already exists",
                            content = @Content(schema = @Schema())
                    )
            }
    )
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Transaction> saveTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Create Transaction request",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Transaction.class)
                    )
            )
            @Valid @RequestBody Transaction transaction) {
        var transactionPersisted = transactionService.createTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionPersisted);
    }
}
