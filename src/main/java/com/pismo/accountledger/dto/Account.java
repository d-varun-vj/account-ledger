package com.pismo.accountledger.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder
@Schema(description = "Represents a customer account")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Account(
        @Schema(description = "Unique identifier for the account", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long accountId,
        @Schema(description = "Unique document number identifying the customer", example = "12345678900")
        @NotEmpty(message = "documentNumber must not be empty")
        String documentNumber,
        @Schema(description = "Account balance amount", example = "100")
        Double balance
) {
}
