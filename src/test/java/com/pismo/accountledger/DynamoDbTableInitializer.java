package com.pismo.accountledger;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

@Component
@Profile("test")
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
                            AttributeDefinition.builder().attributeName("account_id").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("item_id").attributeType(ScalarAttributeType.S).build()
                    )
                    .keySchema(
                            KeySchemaElement.builder().attributeName("account_id").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("item_id").keyType(KeyType.RANGE).build()
                    )
                    .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build())
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
}
