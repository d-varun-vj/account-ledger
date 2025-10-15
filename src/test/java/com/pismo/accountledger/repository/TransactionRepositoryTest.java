package com.pismo.accountledger.repository;

import com.pismo.accountledger.config.ConfigProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionRepositoryTest {
    @Mock
    private ConfigProperties configProperties;
    @Mock
    private DynamoDbClient dynamoDbClient;
    @InjectMocks
    private TransactionRepository transactionRepository;

    @Test
    void testFindByTransactionId_noTransactionReturned() {
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(GetItemResponse.builder().build());
        var result = transactionRepository.findByTransactionId(1, "TXN#1");
        Assertions.assertTrue(result.isEmpty());
    }
}
