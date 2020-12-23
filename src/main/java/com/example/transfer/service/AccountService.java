package com.example.transfer.service;

import com.example.transfer.data.entity.Account;
import com.example.transfer.data.repository.AccountRepository;
import com.example.transfer.vo.AccountTransfer;
import com.example.transfer.vo.AccountTransferResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Long total() {
        return jdbcTemplate.queryForObject("select sum(point) from account", Long.class);
    }

    public Long batchCreate(
            long count
            , int point
    ) {
        return jdbcTemplate.execute((ConnectionCallback<Long>) connection -> {
            PreparedStatement ps = connection.prepareStatement("insert account(point) values(?)");
            long c = 0;
            for (int i = 0; i < count; i++) {
                ps.setInt(1, point);
                c += ps.executeUpdate();
            }
            return c;
        });
    }

    public void truncate(){
        jdbcTemplate.execute("truncate account");
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
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement("update account set point = point - ? where id = ? and point>=?");
            ps.setInt(1, point);
            ps.setInt(2, source);
            ps.setInt(3, point);
            if (ps.executeUpdate() > 0) {
                ps = conn.prepareStatement("update account set point = point + ? where id = ?");
                ps.setInt(1, point);
                ps.setInt(2, target);
                ps.executeUpdate();
            }
            conn.close();
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e2) {

                }
            }
            e.printStackTrace();
        } finally {

        }
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
