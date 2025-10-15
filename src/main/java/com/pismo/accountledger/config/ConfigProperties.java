package com.pismo.accountledger.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
@Setter
@Getter
public class ConfigProperties {

    @Value("${aws.region}")
    private String awsRegion;
    @Value("${aws.dynamodb.endpoint}")
    private String dynamoDbEndpoint;
    @Value("${dynamodb.table.accountledger}")
    private String accountLedgerTable;
    @Value("${dynamodb.table.constraint}")
    private String constraintTable;
    @Value("${dynamodb.table.counter}")
    private String counterTable;
    @Value("${dynamodb.table.txnIdempotency}")
    private String idempotencyTable;

}
