package com.pismo.accountledger.repository;

import com.pismo.accountledger.config.ConfigProperties;
import com.pismo.accountledger.dto.Account;
import com.pismo.accountledger.dto.TypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@RequiredArgsConstructor
public class AccountRepository {
    private static final String ACCOUNT_PREFIX = "ACCOUNT#";

    private final ConfigProperties configProperties;
    private final DynamoDbClient dynamoDbClient;

    //Consider scalable solution
    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    public String createAccount(String documentNumber) {
        String accountId = String.valueOf(COUNTER.getAndIncrement());
        Map<String, AttributeValue> accountLedgerItem = createAccountLedgerItem(documentNumber, accountId);
        Map<String, AttributeValue> accountConstraintItem = createAccountConstraintItem(documentNumber, accountId);
        TransactWriteItemsRequest request = TransactWriteItemsRequest.builder()
                .transactItems(
                        TransactWriteItem.builder()
                                .put(Put.builder()
                                        .tableName(configProperties.getConstraintTable())
                                        .item(accountConstraintItem)
                                        .conditionExpression("attribute_not_exists(constraint_value)")
                                        .build())
                                .build(),
                        TransactWriteItem.builder()
                                .put(Put.builder()
                                        .tableName(configProperties.getAccountLedgerTable())
                                        .item(accountLedgerItem)
                                        .build())
                                .build()
                )
                .build();
        dynamoDbClient.transactWriteItems(request);
        return accountId;
    }

    private Map<String, AttributeValue> createAccountConstraintItem(String documentNumber, String accountId) {
        return Map.of(
                "constraint_type", AttributeValue.fromS("DOCUMENT_NUMBER"),
                "constraint_value", AttributeValue.fromS(documentNumber),
                "account_id", AttributeValue.builder().s(accountId).build()
        );
    }

    private Map<String, AttributeValue> createAccountLedgerItem(String documentNumber, String accountId) {
        return Map.of(
                "account_id", AttributeValue.builder().s(accountId).build(),
                "item_id", AttributeValue.builder().s(ACCOUNT_PREFIX + accountId).build(),
                "type", AttributeValue.builder().s(TypeEnum.ACCOUNT.name()).build(),
                "created_date", AttributeValue.builder().s(LocalDateTime.now(ZoneOffset.UTC).toString()).build(),
                "document_number", AttributeValue.builder().s(documentNumber).build()
        );
    }

    public Optional<Account> getAccount(String accountId) {
        var key = Map.of(
                "account_id", AttributeValue.builder().s(accountId).build(),
                "item_id", AttributeValue.builder().s(ACCOUNT_PREFIX + accountId).build()
        );

        var response = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(configProperties.getAccountLedgerTable())
                .key(key)
                .build());

        if (response.hasItem()) {
            var account = Account.builder()
                    .documentNumber(response.item().get("document_number").s())
                    .accountId(accountId)
                    .build();
            return Optional.of(account);
        } else {
            return Optional.empty();
        }
    }
}
