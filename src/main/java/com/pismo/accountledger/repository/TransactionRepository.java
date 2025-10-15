package com.pismo.accountledger.repository;

import com.pismo.accountledger.config.ConfigProperties;
import com.pismo.accountledger.dto.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TransactionRepository {
    private static final String TRANSACTION_PREFIX = "TRANSACTION#";

    private final ConfigProperties configProperties;
    private final DynamoDbClient dynamoDbClient;

    public void createTransaction(Transaction transaction, double transactionAmount, long transactionId) {
        Map<String, AttributeValue> transactionItem = createTransactionItem(transaction, transactionId, transactionAmount);
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(configProperties.getAccountLedgerTable())
                .item(transactionItem)
                .build();
        dynamoDbClient.putItem(putItemRequest);
    }

    private Map<String, AttributeValue> createTransactionItem(Transaction transaction, long transactionId,
                                                              double transactionAmount) {
        return Map.of(
                "pk", AttributeValue.fromN(transaction.accountId().toString()),
                "sk", AttributeValue.fromS(TRANSACTION_PREFIX + transactionId),
                "operation_type_id", AttributeValue.fromN(String.valueOf(transaction.operationTypeId())),
                "amount", AttributeValue.fromN(String.valueOf(transactionAmount)),
                "event_date", AttributeValue.fromS(LocalDateTime.now(ZoneOffset.UTC).toString())
        );
    }
}
