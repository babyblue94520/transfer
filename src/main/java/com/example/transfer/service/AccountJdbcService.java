package com.example.transfer.service;

import com.example.transfer.vo.AccountTransfer;
import com.example.transfer.vo.AccountTransferResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class AccountJdbcService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * @param source 轉出
     * @param target 轉入
     * @param point  點數
     */
    public void transfer(
            Integer source
            , Integer target
            , Integer point
    ) throws SQLException {
        String subtractSql = "UPDATE account set point = point - ? WHERE id = ?  AND point >= ?";
        String addSql = "UPDATE account set point = point + ? WHERE id = ?";

        int count = jdbcTemplate.update(subtractSql, point, source, point);

        if(count == 1) {
            jdbcTemplate.update(addSql, point, target);
        }

    }

    public AccountTransferResult transfer2(
            Integer source
            , Integer target
            , Integer point
    ) {
        final int[] resultPoint = { 0,0,0,0 };
        String subtractSql = "UPDATE account set point = @sa := ((@sb := point) - ?) WHERE id = ? AND point >= ?";
        String addSql = "UPDATE account set  point = @ta := ((@tb := point) + ?) WHERE id = ?";
        String resultSql = "SELECT @sb,@sa,@tb,@ta";

        boolean result = jdbcTemplate.execute(subtractSql, new PreparedStatementCallback<Boolean>() {
            @Override
            public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.setInt(1, point);
                ps.setInt(2, source);
                ps.setInt(3, point);
                int count = ps.executeUpdate();

                if(count == 1) {
                    ps = ps.getConnection().prepareStatement(addSql);
                    ps.setInt(1, point);
                    ps.setInt(2, target);
                    ps.executeUpdate();
                    ResultSet rs = ps.getConnection().createStatement().executeQuery(resultSql);
                    while (rs.next()){
                        resultPoint[0] = rs.getInt(1);
                        resultPoint[1] = rs.getInt(2);
                        resultPoint[2] = rs.getInt(3);
                        resultPoint[3] = rs.getInt(4);
                    }
                    return true;
                }
                return false;
            }
        });

        return new AccountTransferResult(
                result
                , point
                , new AccountTransfer(source, resultPoint[0], resultPoint[1])
                , new AccountTransfer(target, resultPoint[2], resultPoint[3])
        );
    }
}
