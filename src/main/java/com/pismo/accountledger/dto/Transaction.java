package com.pismo.accountledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.beans.Transient;
import java.time.LocalDateTime;

@Builder
@Schema(description = "Represents customer transaction")
public record Transaction(
        @Schema(description = "Unique identifier for a transaction", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long transactionId,
        @NotNull(message = "Invalid accountId")
        @Schema(description = "Unique identifier for a customer account", example = "1")
        Long accountId,
        @Schema(description = "Represent the transaction amount", example = "1")
        @NotNull(message = "Account id must be valid")
        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
        double amount,
        @Schema(description = "Identifier that represents transaction type", example = "1")
        @NotNull(message = "Invalid operation type ID")
        Long operationTypeId,
        @Transient
        @Schema(description = "Represent the transaction amount", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime eventDate) {
}
