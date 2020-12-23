package com.example.transfer.config;

import com.example.transfer.service.AccountService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;


@Log4j2
@Configuration
public class AccountConfig implements CommandLineRunner {
    public static final int AccountCount = 10000;
    public static final int DefaultPoint = 10000;

    @Autowired
    private AccountService accountService;

    @Override
    public void run(String... args) {
//        Long t = System.currentTimeMillis();
//        log.info("insert {} account {}ms"
//                , accountService.batchCreate(AccountCount, DefaultPoint)
//                , System.currentTimeMillis() - t)
//        ;
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        Class<?> clazz = list.getClass();

        Type type = clazz;

        System.out.println(clazz.getTypeParameters());
        System.out.println(((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0]);
    }
}
