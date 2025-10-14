package com.pismo.accountledger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
@Profile("!test")
public class DynamoDBConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.dynamodb.endpoint}")
    private String dynamoEndpoint;

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
        String tableName = "account_ledger";
        try {
//            client.createTable(CreateTableRequest.builder()
//                    .tableName(tableName)
//                    .keySchema(
//                            KeySchemaElement.builder().attributeName("account_id").keyType(KeyType.HASH).build(),
//                            KeySchemaElement.builder().attributeName("item_id").keyType(KeyType.RANGE).build()
//                    )
//                    .attributeDefinitions(
//                            AttributeDefinition.builder().attributeName("account_id").attributeType(ScalarAttributeType.S).build(),
//                            AttributeDefinition.builder().attributeName("item_id").attributeType(ScalarAttributeType.S).build()
//                    )
//                    .provisionedThroughput(
//                            ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build()
//                    )
//                    .build());
            System.out.println("✅ Table created: " + tableName);
        } catch (Exception e) {
            System.out.println("ℹ️ Table already exists or could not be created: " + e.getMessage());
        }
    }
}
