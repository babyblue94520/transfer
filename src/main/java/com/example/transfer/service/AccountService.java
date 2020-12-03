package com.example.transfer.service;

import com.example.transfer.data.entity.Account;
import com.example.transfer.data.repository.AccountRepository;
import com.example.transfer.vo.AccountTransfer;
import com.example.transfer.vo.AccountTransferResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DataSource dataSource;

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
    )  {
        String subtractSql = "UPDATE account set point = point - ? WHERE id = ?  AND point >= ?";
        String addSql = "UPDATE account set point = point + ? WHERE id = ?";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(subtractSql);
            ps.setInt(1, point);
            ps.setInt(2, source);
            ps.setInt(3, point);
            int count = ps.executeUpdate();

            if(count == 1) {
                ps = connection.prepareStatement(addSql);
                ps.setInt(1, point);
                ps.setInt(2, target);
                ps.executeUpdate();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if(connection != null) {
                try {
                    connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }

    public AccountTransferResult transfer2(
            Integer source
            , Integer target
            , Integer point
    ) {
        int sourceBeforePoint = 0, sourceAfterPoint = 0, targetBeforePoint = 0, targetAfterPoint = 0;
        String subtractSql = "UPDATE account set point = @sa := ((@sb := point) - ?) WHERE id = ? AND point >= ?";
        String addSql = "UPDATE account set  point = @ta := ((@tb := point) + ?) WHERE id = ?";
        String resultSql = "SELECT @sb,@sa,@tb,@ta";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(subtractSql);
            ps.setInt(1, point);
            ps.setInt(2, source);
            ps.setInt(3, point);
            int count = ps.executeUpdate();


            if(count == 1) {
                ps = connection.prepareStatement(addSql);
                ps.setInt(1, point);
                ps.setInt(2, target);
                ps.executeUpdate();
                ResultSet rs = connection.createStatement().executeQuery(resultSql);
                while (rs.next()){
                    sourceBeforePoint = rs.getInt(1);
                    sourceAfterPoint = rs.getInt(2);
                    targetBeforePoint = rs.getInt(3);
                    targetAfterPoint = rs.getInt(4);
                }
                rs.close();
            } else {
                return new AccountTransferResult(
                        false
                        , point
                        , null
                        , null
                );
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if(connection != null) {
                try {
                    connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }

        return new AccountTransferResult(
                true
                , point
                , new AccountTransfer(source, sourceBeforePoint, sourceAfterPoint)
                , new AccountTransfer(target, targetBeforePoint, targetAfterPoint)
        );
    }
}
