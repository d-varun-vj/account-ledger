package com.pismo.accountledger.service;

import com.pismo.accountledger.dto.Account;

public interface AccountService {
    Account createAccount(String documentNumber);
    Account getAccount(String accountId);
}
