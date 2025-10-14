package com.pismo.accountledger.config;

import com.pismo.accountledger.dto.enums.TypeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

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
                            KeySchemaElement.builder().attributeName("pk").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("sk").keyType(KeyType.RANGE).build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("pk").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("sk").attributeType(ScalarAttributeType.S).build()
                    )
                    .provisionedThroughput(
                            ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build()
                    )
                    .build());
            client.batchWriteItem(BatchWriteItemRequest.builder()
                            .requestItems(Map.of(accountLedgerTable, List.of(
                                            WriteRequest.builder()
                                                    .putRequest(PutRequest.builder().item(createAccountLedgerItem("1", "PURCHASE", false)).build())
                                                    .build(),
                                            WriteRequest.builder()
                                                    .putRequest(PutRequest.builder().item(createAccountLedgerItem("2", "INSTALLMENT PURCHASE", false)).build())
                                                    .build(),
                                            WriteRequest.builder()
                                                    .putRequest(PutRequest.builder().item(createAccountLedgerItem("3", "WITHDRAWAL", false)).build())
                                                    .build(),
                                            WriteRequest.builder()
                                                    .putRequest(PutRequest.builder().item(createAccountLedgerItem("4", "PAYMENT", true)).build())
                                                    .build())))
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

    private Map<String, AttributeValue> createAccountLedgerItem(String id, String operationType, boolean isCredit) {
        return Map.of(
                "pk", AttributeValue.fromS(id),
                "sk", AttributeValue.fromS(TypeEnum.OPERATION.name() + "#" + operationType.replace(" ", "")),
                "type", AttributeValue.fromS(operationType),
                "created_date", AttributeValue.fromS(LocalDateTime.now(ZoneOffset.UTC).toString()),
                "credit", AttributeValue.fromBool(isCredit)
        );
    }
}
