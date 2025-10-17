package com.pismo.accountledger.repository;

import com.pismo.accountledger.config.ConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountConstraintRepository {

    private final ConfigProperties configProperties;
    private final DynamoDbClient dynamoDbClient;

    public boolean isAccountDocExist(String documentNumber) {
        return false;
    }
}
