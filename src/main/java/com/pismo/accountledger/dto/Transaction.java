package com.pismo.accountledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Represents customer transaction")
public record Transaction(
        @Schema(description = "Unique identifier for a transaction", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        String transactionId,
        @Schema(description = "Unique identifier for a customer account", example = "1")
        String accountId,
        @Schema(description = "Represent the transaction amount", example = "1")
        double amount,
        @Schema(description = "Identifier that represents transaction type", example = "1")
        String operationTypeId,
        @Schema(description = "Represent the transaction amount", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime eventDate) {
}
