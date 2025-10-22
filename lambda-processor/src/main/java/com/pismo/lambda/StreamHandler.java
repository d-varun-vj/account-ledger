package com.pismo.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class StreamHandler implements RequestHandler<DynamodbEvent, String> {
    private static final Logger log = LoggerFactory.getLogger(StreamHandler.class);
    private final DynamoDbClient dynamoDb;
    public StreamHandler() {
        this.dynamoDb = DynamoDbClient.builder()
                .endpointOverride(URI.create("http://localstack:4566"))
                .region(Region.of("ap-south-1"))
                .build();
    }

    private static final String ACCOUNTS_LEDGER_TABLE = "account_ledger";


    @Override
    public String handleRequest(DynamodbEvent event, Context context) {
        log.info("Received {} records", event.getRecords().size());

        for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
            log.info("record.getEventName() {}", record.getEventName());
            if (!"INSERT".equals(record.getEventName())) continue;
            var newImage = record.getDynamodb().getNewImage();
            log.info("newImage {}", newImage);
            if (newImage == null || !(newImage.containsKey("type") && newImage.get("type").getS().equals("TRANSACTION"))) continue;
            String accountId = newImage.get("pk").getN();
            String transactionId = newImage.get("sk").getS();
            double amount = Double.parseDouble(newImage.get("amount").getN());
            log.info("process record accountId {}, transactionId {}, amount {}", accountId, transactionId, amount);
            if (amount >= 0) {
                log.info("Deposit amount {}", amount);
                deposit(accountId, amount);
            } else {
                log.info("Withdraw amount {}", amount);
                withdraw(accountId, amount);
            }
        }

        return "Processed " + event.getRecords().size() + " records.";
    }

    private void deposit(String accountId, double amount) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("pk", AttributeValue.fromN(accountId));
        key.put("sk", AttributeValue.fromS("ACCOUNT#" + accountId));

        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":amount", AttributeValue.fromN(String.valueOf(amount)));
        values.put(":zero", AttributeValue.fromN("0"));

        UpdateItemRequest update = UpdateItemRequest.builder()
                .tableName(ACCOUNTS_LEDGER_TABLE)
                .key(key)
                .updateExpression("SET balance = if_not_exists(balance, :zero) + :amount")
                .expressionAttributeValues(values)
                .build();

        dynamoDb.updateItem(update);
        log.info("💰 Deposited {} to {}", amount, accountId);
    }

    private void withdraw(String accountId, double amount) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("pk", AttributeValue.fromN(accountId));
        key.put("sk", AttributeValue.fromS("ACCOUNT#" + accountId));
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":amount", AttributeValue.fromN(String.valueOf(amount)));
        values.put(":zero", AttributeValue.fromN("0"));

        UpdateItemRequest update = UpdateItemRequest.builder()
                .tableName(ACCOUNTS_LEDGER_TABLE)
                .key(key)
                .updateExpression("SET balance = if_not_exists(balance, :zero) + :amount")
                .conditionExpression("attribute_exists(balance) and balance >= :amount")
                .expressionAttributeValues(values)
                .build();

        dynamoDb.updateItem(update);
        log.info("✅ Updated balance for {} by {}", accountId, amount);
    }
}
