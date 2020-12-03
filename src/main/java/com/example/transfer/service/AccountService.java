package com.example.transfer.service;

import com.example.transfer.data.entity.Account;
import com.example.transfer.data.repository.AccountRepository;
import com.example.transfer.vo.AccountTransfer;
import com.example.transfer.vo.AccountTransferResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    /**
     * @param source 轉出
     * @param target 轉入
     * @param point  點數
     */
    public void transfer(
            Integer source
            , Integer target
            , Integer point
    ) {
        // TODO 任何方式實作都可以，不限於使用 JPA
    }

    public AccountTransferResult transfer2(
            Integer source
            , Integer target
            , Integer point
    ) {
        int sourceBeforePoint = 0, sourceAfterPoint = 0, targetBeforePoint = 0, targetAfterPoint = 0;
        // TODO 任何方式實作都可以，不限於使用 JPA

        return new AccountTransferResult(
                true
                , point
                , new AccountTransfer(source, sourceBeforePoint, sourceAfterPoint)
                , new AccountTransfer(target, targetBeforePoint, targetAfterPoint)
        );
    }
}
