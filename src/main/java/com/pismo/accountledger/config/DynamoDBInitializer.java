package com.pismo.accountledger.config;

import com.pismo.accountledger.dto.enums.TypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynamoDBInitializer {

    private final ConfigProperties configProperties;
    private final DynamoDbClient client;

    @EventListener(ApplicationReadyEvent.class)
    public void createTableIfNotExists() {
        createAccountLedgerTable(configProperties.getAccountLedgerTable(), "pk", "sk");
        insertOperationTypes();
        createCounterTable();
        insertToCounterIfNotExists(TypeEnum.ACCOUNT.name());
        insertToCounterIfNotExists(TypeEnum.TRANSACTION.name());
        createConstraintTable(configProperties.getConstraintTable(), "constraint_type", "constraint_value");
    }

    private void createCounterTable() {
        try {
            client.createTable(CreateTableRequest.builder()
                    .tableName(configProperties.getCounterTable())
                    .keySchema(
                            KeySchemaElement.builder().attributeName("counter_name").keyType(KeyType.HASH).build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("counter_name").attributeType(ScalarAttributeType.S).build()
                    )
                    .provisionedThroughput(
                            ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build()
                    )
                    .build());
        } catch (ConditionalCheckFailedException e) {
            log.error("counter table already exist");
        }
    }

    private void insertOperationTypes() {
        client.batchWriteItem(BatchWriteItemRequest.builder()
                .requestItems(Map.of(configProperties.getAccountLedgerTable(), List.of(
                        WriteRequest.builder()
                                .putRequest(PutRequest.builder().item(createAccountLedgerItem(1, "PURCHASE", false)).build())
                                .build(),
                        WriteRequest.builder()
                                .putRequest(PutRequest.builder().item(createAccountLedgerItem(2, "INSTALLMENT PURCHASE", false)).build())
                                .build(),
                        WriteRequest.builder()
                                .putRequest(PutRequest.builder().item(createAccountLedgerItem(3, "WITHDRAWAL", false)).build())
                                .build(),
                        WriteRequest.builder()
                                .putRequest(PutRequest.builder().item(createAccountLedgerItem(4, "PAYMENT", true)).build())
                                .build())))
                .build());
    }

    private void createAccountLedgerTable(String configProperties, String pk, String sk) {
        try {
            client.createTable(CreateTableRequest.builder()
                    .tableName(configProperties)
                    .keySchema(
                            KeySchemaElement.builder().attributeName(pk).keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName(sk).keyType(KeyType.RANGE).build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName(pk).attributeType(ScalarAttributeType.N).build(),
                            AttributeDefinition.builder().attributeName(sk).attributeType(ScalarAttributeType.S).build()
                    )
                    .provisionedThroughput(
                            ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build()
                    )
                    .build());
        } catch (ConditionalCheckFailedException e) {
            log.error("account ledger table already exist");
        }
    }

    private void createConstraintTable(String configProperties, String pk, String sk) {
        try {
            client.createTable(CreateTableRequest.builder()
                    .tableName(configProperties)
                    .keySchema(
                            KeySchemaElement.builder().attributeName(pk).keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName(sk).keyType(KeyType.RANGE).build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName(pk).attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName(sk).attributeType(ScalarAttributeType.S).build()
                    )
                    .provisionedThroughput(
                            ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build()
                    )
                    .build());
        } catch (ConditionalCheckFailedException e) {
            log.error("account ledger table already exist");
        }
    }

    public void insertToCounterIfNotExists(String counterName) {
        try {
            client.putItem(PutItemRequest.builder()
                    .tableName(configProperties.getCounterTable())
                    .item(Map.of(
                            "counter_name", AttributeValue.fromS(counterName),
                            "last_id", AttributeValue.fromN("0")
                    ))
                    .conditionExpression("attribute_not_exists(counter_name)")
                    .build());
        } catch (ConditionalCheckFailedException e) {
            log.error("counter already exist");
        }
    }

    private Map<String, AttributeValue> createAccountLedgerItem(long id, String operationType, boolean isCredit) {
        return Map.of(
                "pk", AttributeValue.fromN(String.valueOf(id)),
                "sk", AttributeValue.fromS(TypeEnum.OPERATION.name() + "#" + id),
                "type", AttributeValue.fromS(operationType),
                "created_date", AttributeValue.fromS(LocalDateTime.now(ZoneOffset.UTC).toString()),
                "credit", AttributeValue.fromBool(isCredit)
        );
    }
}
