package com.pismo.accountledger.dto;

import java.time.LocalDateTime;

public record Transaction(String transactionId,
                          String accountId,
                          String operationType,
                          double amount,
                          LocalDateTime eventDate) {
}
