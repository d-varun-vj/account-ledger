package com.pismo.accountledger.repository;

import com.pismo.accountledger.config.ConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionIdempotencyRepository {
    private final ConfigProperties configProperties;
    private final DynamoDbClient dynamoDbClient;

    public Optional<String> getTransactionId(String idempotencyKey) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(configProperties.getIdempotencyTable())
                .key(Map.of("idempotency_key", AttributeValue.fromS(idempotencyKey)))
                .build();

        Map<String, AttributeValue> item = dynamoDbClient.getItem(request).item();
        if (item == null || item.isEmpty()) return Optional.empty();
        return Optional.of(item.get("transaction_id").s());
    }
}
