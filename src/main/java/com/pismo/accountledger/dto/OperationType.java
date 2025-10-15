package com.pismo.accountledger.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OperationType (
        String operationType,
        boolean isCredit
){

}
