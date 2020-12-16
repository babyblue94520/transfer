package com.example.transfer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;


@Configuration
public class AccountConfig implements CommandLineRunner {
    public static final int AccountCount = 10000;
    public static final int DefaultPoint = 10000;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        for (int i = 1; i <= AccountCount; i++) {
            jdbcTemplate.update("insert account(point) values(?)", DefaultPoint);
        }
    }
}
