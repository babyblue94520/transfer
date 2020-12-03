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
        String sourceSql = "update Account set point = point - ? where id = ? and point > ?";
        String targetSql = "update Account set point = point + ? where id = ?";
        jdbcTemplate.execute(sourceSql, (PreparedStatementCallback<Integer>) ps -> {
            ps.setInt(1, point);
            ps.setInt(2, source);
            ps.setInt(3, point);
            if (ps.executeUpdate() > 0) {
                ps.clearParameters();

                ps = ps.getConnection().prepareStatement(targetSql);
                ps.setInt(1, point);
                ps.setInt(2, target);
                return ps.executeUpdate();
            }
            return 0;
        });
    }

    public AccountTransferResult transfer2(
            Integer source
            , Integer target
            , Integer point
    ) {
        int sourceBeforePoint = 0, sourceAfterPoint = 0, targetBeforePoint = 0, targetAfterPoint = 0;
        // TODO 任何方式實作都可以，不限於使用 JPA
        String sourceSql = "update Account set point = @sourceAfterPoint := (@sourceBeforePoint := point) - ? where id = ? and point > ?;";
        String targetSql = "update Account set point = @targetAfterPoint := (@targetBeforePoint := point) + ? where id = ?;";
        String resultSql = "select @sourceBeforePoint, @sourceAfterPoint, @targetBeforePoint, @targetAfterPoint;";

        Map<String, Integer> map = new HashMap<>();
        int[] arr = {0, 0, 0, 0};
        jdbcTemplate.execute(sourceSql, (PreparedStatementCallback<Integer>) ps -> {
            ps.setInt(1, point);
            ps.setInt(2, source);
            ps.setInt(3, point);
            if (ps.executeUpdate() > 0) {
                ps.clearParameters();
                ps = ps.getConnection().prepareStatement(targetSql);
                ps.setInt(1, point);
                ps.setInt(2, target);
                ps.executeUpdate();
            }

            ps.clearParameters();
            ps = ps.getConnection().prepareStatement(resultSql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                arr[0] = rs.getInt("@sourceBeforePoint");
                arr[1] = rs.getInt("@sourceAfterPoint");
                arr[2] = rs.getInt("@targetBeforePoint");
                arr[3] = rs.getInt("@targetAfterPoint");
                return 4;
            }
            return 0;
        });
        sourceBeforePoint = arr[0];
        sourceAfterPoint = arr[1];
        targetBeforePoint = arr[2];
        targetAfterPoint = arr[3];
        return new AccountTransferResult(
                true
                , point
                , new AccountTransfer(source, sourceBeforePoint, sourceAfterPoint)
                , new AccountTransfer(target, targetBeforePoint, targetAfterPoint)
        );
    }
}
