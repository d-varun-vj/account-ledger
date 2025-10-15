package com.pismo.accountledger.repository;

import com.pismo.accountledger.config.ConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CounterRepository {

    private final ConfigProperties configProperties;
    private final DynamoDbClient dynamoDbClient;

    public long getNextId(String counterName) {
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(configProperties.getCounterTable())
                .key(Map.of("counter_name", AttributeValue.fromS(counterName)))
                .updateExpression("ADD last_id :incr")
                .expressionAttributeValues(Map.of(":incr", AttributeValue.fromN("1")))
                .returnValues(ReturnValue.UPDATED_NEW)
                .build();

        UpdateItemResponse response = dynamoDbClient.updateItem(request);
        return Long.parseLong(response.attributes().get("last_id").n());
    }
}
