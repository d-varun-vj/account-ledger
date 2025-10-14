package com.pismo.accountledger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.net.URI;

@Configuration
@Profile("!test")
public class DynamoDBConfig {

    @Value("${aws.region}")
    private String region;
    @Value("${aws.dynamodb.endpoint}")
    private String dynamoEndpoint;
    @Value("${dynamodb.table.accountledger}")
    private String accountLedgerTable;
    @Value("${dynamodb.table.constraint}")
    private String constraintTable;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        DynamoDbClient client = DynamoDbClient.builder()
                .endpointOverride(URI.create(dynamoEndpoint))
                .region(Region.of(region))
                .build();

        createTableIfNotExists(client);
        return client;
    }

    private void createTableIfNotExists(DynamoDbClient client) {
        try {
            client.describeTable(DescribeTableRequest.builder()
                    .tableName(accountLedgerTable)
                    .build());
        } catch (Exception ex) {
            client.createTable(CreateTableRequest.builder()
                    .tableName(accountLedgerTable)
                    .keySchema(
                            KeySchemaElement.builder().attributeName("account_id").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("item_id").keyType(KeyType.RANGE).build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("account_id").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("item_id").attributeType(ScalarAttributeType.S).build()
                    )
                    .provisionedThroughput(
                            ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build()
                    )
                    .build());
            client.createTable(CreateTableRequest.builder()
                    .tableName(constraintTable)
                    .keySchema(
                            KeySchemaElement.builder().attributeName("constraint_type").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("constraint_value").keyType(KeyType.RANGE).build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("constraint_type").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("constraint_value").attributeType(ScalarAttributeType.S).build()
                    )
                    .provisionedThroughput(
                            ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build()
                    )
                    .build());
            System.out.println("Table created: ");
        }
    }
}
