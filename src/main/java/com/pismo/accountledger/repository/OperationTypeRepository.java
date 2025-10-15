package com.pismo.accountledger.repository;

import com.pismo.accountledger.config.ConfigProperties;
import com.pismo.accountledger.dto.OperationType;
import com.pismo.accountledger.dto.enums.TypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OperationTypeRepository {

    private final DynamoDbClient dynamoDbClient;
    private final ConfigProperties configProperties;
    private static final String OPERATION_TYPE_PREFIX = TypeEnum.OPERATION.name() + "#";

    public Optional<OperationType> getOperationById(long operationTypeId) {
        var key = Map.of(
                "pk", AttributeValue.builder().n(String.valueOf(operationTypeId)).build(),
                "sk", AttributeValue.builder().s(OPERATION_TYPE_PREFIX + operationTypeId).build()
        );
        var response = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(configProperties.getAccountLedgerTable())
                .key(key)
                .build());
        if (response.hasItem()) {
            return Optional.of(OperationType.builder()
                            .operationType(response.item().get("type").s())
                            .isCredit(response.item().get("credit").bool())
                    .build());
        }
        return Optional.empty();
    }
}
