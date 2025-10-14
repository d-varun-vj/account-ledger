package com.pismo.accountledger.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Represents a customer account")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Account(
        @Schema(description = "Unique identifier for the account", example = "1")
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        String accountId,

        @Schema(description = "Unique document number identifying the customer", example = "12345678900")
        String documentNumber
) {
}
