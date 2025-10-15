package com.pismo.accountledger;

import com.pismo.accountledger.dto.enums.TypeEnum;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

//@Component
//@Profile("test")
public class DynamoDbTableInitializer {
    private final DynamoDbClient dynamoDbClient;

    public DynamoDbTableInitializer(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createTable() {
        try {
            dynamoDbClient.createTable(CreateTableRequest.builder()
                    .tableName("account_ledger")
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("pk").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("sk").attributeType(ScalarAttributeType.S).build()
                    )
                    .keySchema(
                            KeySchemaElement.builder().attributeName("pk").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("sk").keyType(KeyType.RANGE).build()
                    )
                    .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build())
                    .build());
            dynamoDbClient.batchWriteItem(BatchWriteItemRequest.builder()
                    .requestItems(Map.of("account_ledger", List.of(
                            WriteRequest.builder()
                                    .putRequest(PutRequest.builder().item(createAccountLedgerOperationItem("1", "PURCHASE", false)).build())
                                    .build(),
                            WriteRequest.builder()
                                    .putRequest(PutRequest.builder().item(createAccountLedgerOperationItem("2", "INSTALLMENT PURCHASE", false)).build())
                                    .build(),
                            WriteRequest.builder()
                                    .putRequest(PutRequest.builder().item(createAccountLedgerOperationItem("3", "WITHDRAWAL", false)).build())
                                    .build(),
                            WriteRequest.builder()
                                    .putRequest(PutRequest.builder().item(createAccountLedgerOperationItem("4", "PAYMENT", true)).build())
                                    .build())))
                    .build());
            System.out.println("Table created. Name: account_ledger");
            dynamoDbClient.createTable(CreateTableRequest.builder()
                    .tableName("account_unique_constraint")
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("constraint_type").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("constraint_value").attributeType(ScalarAttributeType.S).build()
                    )
                    .keySchema(
                            KeySchemaElement.builder().attributeName("constraint_type").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("constraint_value").keyType(KeyType.RANGE).build()
                    )
                    .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build())
                    .build());
            System.out.println("Table created. Name: account_unique_constraint");
            System.out.println("DynamoDB table created!");
        } catch (ResourceInUseException e) {
            System.out.println("Table already exists");
        }
    }

    private Map<String, AttributeValue> createAccountLedgerOperationItem(String id, String operationType, boolean isCredit) {
        return Map.of(
                "pk", AttributeValue.fromS(id),
                "sk", AttributeValue.fromS(TypeEnum.OPERATION.name() + "#" + id),
                "type", AttributeValue.fromS(operationType),
                "created_date", AttributeValue.fromS(LocalDateTime.now(ZoneOffset.UTC).toString()),
                "credit", AttributeValue.fromBool(isCredit)
        );
    }
}
