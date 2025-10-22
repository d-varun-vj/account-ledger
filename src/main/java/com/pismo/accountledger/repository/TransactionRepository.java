package com.pismo.accountledger.repository;

import com.pismo.accountledger.config.ConfigProperties;
import com.pismo.accountledger.dto.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;
import software.amazon.awssdk.services.dynamodb.model.Update;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionRepository {
    private static final String TRANSACTION_PREFIX = "TRANSACTION#";

    private final ConfigProperties configProperties;
    private final DynamoDbClient dynamoDbClient;

    public void saveIdempotentTransaction(Transaction transaction, double transactionAmount, long transactionId,
                                          String idempotencyKey) {
        Map<String, AttributeValue> expressionAttributeValue = expressionAttribute(transaction.operationTypeId(), transaction.amount());
        Map<String, AttributeValue> idempotencyItem = createIdempotentItem(idempotencyKey, transactionId);
        Map<String, AttributeValue> transactionItem = createTransactionItem(transaction, transactionId, transactionAmount);
        TransactWriteItemsRequest request = TransactWriteItemsRequest.builder()
                .transactItems(
                        TransactWriteItem.builder()
                                .put(Put.builder()
                                        .tableName(configProperties.getIdempotencyTable())
                                        .item(idempotencyItem)
                                        .conditionExpression("attribute_not_exists(idempotency_key)")
                                        .build())
                                .build(),
                        TransactWriteItem.builder()
                                        .update(Update.builder()
                                                .tableName(configProperties.getAccountLedgerTable())
                                                .key(Map.of("pk", AttributeValue.fromN(String.valueOf(transaction.accountId())),
                                                        "sk", AttributeValue.fromS("ACCOUNT#" + transaction.accountId())))
                                                .updateExpression(" SET balance = balance + :amount")
                                                .conditionExpression("attribute_exists(balance) AND balance >= :threshold")// -1000 >= -800 + -300
                                                .expressionAttributeValues(expressionAttributeValue)
                                                .build())
                                .build(),
                        TransactWriteItem.builder()
                                .put(Put.builder()
                                        .tableName(configProperties.getAccountLedgerTable())
                                        .item(transactionItem)
                                        .build())

                                .build()
                )
                .build();
        dynamoDbClient.transactWriteItems(request);
    }

    private Map<String, AttributeValue> expressionAttribute(Long operationTypeId, double amount) {
        if (operationTypeId != 4) {
            amount = -amount;
        }
        double threshold = - configProperties.getCreditLimit() - amount;
        return Map.of(
                ":amount", AttributeValue.fromN(String.valueOf(amount)),
                ":threshold", AttributeValue.fromN(String.valueOf(threshold)));
    }

    private Map<String, AttributeValue> createIdempotentItem(String idempotencyKey, long transactionId) {
        return Map.of(
                "idempotency_key", AttributeValue.fromS(idempotencyKey),
                "transaction_id", AttributeValue.fromS("TRANSACTION#" + transactionId),
                "create_date", AttributeValue.fromS(LocalDateTime.now(ZoneOffset.UTC).toString())
        );
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

    public Optional<Transaction> findByTransactionId(long accountId, String transactionId) {
        var key = Map.of(
                "pk", AttributeValue.builder().n(String.valueOf(accountId)).build(),
                "sk", AttributeValue.builder().s(transactionId).build()
        );

        var response = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(configProperties.getAccountLedgerTable())
                .key(key)
                .build());

        if (response.hasItem()) {
            var transaction = Transaction.builder()
                    .transactionId(Long.valueOf(transactionId.substring(TRANSACTION_PREFIX.length())))
                    .accountId(accountId)
                    .amount(Double.parseDouble(response.item().get("amount").n()))
                    .operationTypeId(Long.parseLong(response.item().get("operation_type_id").n()))
                    .build();
            return Optional.of(transaction);
        } else {
            return Optional.empty();
        }
    }
}
