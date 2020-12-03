package com.example.transfer.controller;

import com.example.transfer.config.AccountConfig;
import com.example.transfer.data.entity.Account;
import com.example.transfer.result.AllResult;
import com.example.transfer.service.AccountService;
import com.example.transfer.vo.AccountTransfer;
import com.example.transfer.vo.AccountTransferResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.*;

@Api(tags = "帳戶")
@RestController
@RequestMapping("account")
public class AccountController {
    public static final int AccountCount = 10;
    public static final int DefaultPoint = 1000;

    @Autowired
    public AccountService accountService;

    private AllResult result() {
        List<Account> accounts = accountService.findAll();
        Long total = 0L;
        for (Account account : accounts) {
            total += account.getPoint();
        }
        return new AllResult(accounts, total);
    }

    @ApiOperation(value = "查詢所有帳戶")
    @GetMapping("all")
    public AllResult findAll() {
        return result();
    }

    @ApiOperation(value = "轉帳")
    @PostMapping("transfer")
    public AllResult transfer(
            @ApiParam(value = "轉出", example = "1")
            @RequestParam Integer source
            , @ApiParam(value = "轉入", example = "2")
            @RequestParam Integer target
            , @ApiParam(value = "點數", example = "100")
            @RequestParam Integer point
    ) {
        accountService.transfer(source, target, point);
        return result();
    }

    @ApiOperation(value = "轉帳-返回交易明細")
    @PostMapping("transfer2")
    public AccountTransferResult transfer2(
            @ApiParam(value = "轉出", example = "1")
            @RequestParam Integer source
            , @ApiParam(value = "轉入", example = "2")
            @RequestParam Integer target
            , @ApiParam(value = "點數", example = "100")
            @RequestParam Integer point
    ) {
        return accountService.transfer2(source, target, point);
    }

    @ApiOperation(value = "測試轉帳")
    @PostMapping("transfer/test")
    public String transfer(
            @ApiParam(value = "執行緒數量", example = "8")
            @RequestParam Integer thread
            , @ApiParam(value = "交易次數", example = "10000")
            @RequestParam Integer count
            , @ApiParam(value = "最小交易點數", example = "1")
            @RequestParam Integer minPoint
            , @ApiParam(value = "最大交易點數", example = "100")
            @RequestParam Integer maxPoint
    ) throws Exception {
        Random random = new Random();
        ExecutorService executorService = Executors.newFixedThreadPool(thread);
        List<Callable<Integer>> tasks = new ArrayList<>();
        int max = maxPoint - minPoint;
        for (int i = 0; i < thread; i++) {
            tasks.add(() -> {
                for (int j = 0; j < count; j++) {
                    accountService.transfer(
                            random.nextInt(AccountConfig.AccountCount) + 1
                            , random.nextInt(AccountConfig.AccountCount) + 1
                            , random.nextInt(max) + minPoint
                    );
                }
                return count;
            });
        }
        long t = System.currentTimeMillis();
        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        int total = 0;
        for (Future<Integer> future : futures) {
            total += future.get();
        }
        long ms = System.currentTimeMillis() - t;

        List<Account> accounts = accountService.findAll();
        StringBuilder detail = new StringBuilder();
        Long totalPoint = 0L;
        for (Account account : accounts) {
            totalPoint += account.getPoint();
            detail.append(account.getId())
                    .append('>')
                    .append(account.getPoint())
                    .append('\n');
        }
        return String.format("交易次數：%s\n耗費時間：%d\n每秒交易次數：%d\n帳戶總額；%d\n明細：\n%s"
                , total
                , ms
                , (total * 1000 / ms)
                , totalPoint
                , detail.toString()
        );
    }


    @ApiOperation(value = "測試轉帳-返回交易明細")
    @PostMapping("transfer2/test")
    public String transfer2(
            @ApiParam(value = "執行緒數量", example = "8")
            @RequestParam Integer thread
            , @ApiParam(value = "交易次數", example = "10000")
            @RequestParam Integer count
            , @ApiParam(value = "最小交易點數", example = "1")
            @RequestParam Integer minPoint
            , @ApiParam(value = "最大交易點數", example = "100")
            @RequestParam Integer maxPoint
    ) throws Exception {
        Random random = new Random();
        ExecutorService executorService = Executors.newFixedThreadPool(thread);
        List<Callable<Integer>> tasks = new ArrayList<>();
        int max = maxPoint - minPoint;
        for (int i = 0; i < thread; i++) {
            tasks.add(() -> {
                AccountTransferResult result;
                for (int j = 0; j < count; j++) {
                    result = accountService.transfer2(
                            random.nextInt(AccountConfig.AccountCount) + 1
                            , random.nextInt(AccountConfig.AccountCount) + 1
                            , random.nextInt(max) + minPoint
                    );
                    if (result.getStatus()
                            && !(Objects.equals(result.getSource().getBefore() - result.getPoint(), result.getSource().getAfter())
                                    && Objects.equals(result.getTarget().getBefore() + result.getPoint(), result.getTarget().getAfter()) )
                    ) {
                        throw new RuntimeException(String.format("交易結果異常：%s", result));
                    }
                }
                return count;
            });
        }
        int total = 0;
        long t = System.currentTimeMillis();
        try {
            List<Future<Integer>> futures = executorService.invokeAll(tasks);
            for (Future<Integer> future : futures) {
                total += future.get();
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        long ms = System.currentTimeMillis() - t;
        List<Account> accounts = accountService.findAll();
        StringBuilder detail = new StringBuilder();
        Long totalPoint = 0L;
        for (Account account : accounts) {
            totalPoint += account.getPoint();
            detail.append(account.getId())
                    .append('>')
                    .append(account.getPoint())
                    .append('\n');
        }
        return String.format("交易次數：%s\n耗費時間：%d\n每秒交易次數：%d\n帳戶總額；%d\n明細：\n%s"
                , total
                , ms
                , (total * 1000 / ms)
                , totalPoint
                , detail.toString()
        );
    }
}
