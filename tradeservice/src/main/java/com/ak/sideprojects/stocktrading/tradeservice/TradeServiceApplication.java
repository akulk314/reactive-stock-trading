package com.ak.sideprojects.stocktrading.tradeservice;

import com.ak.sideprojects.stocktrading.tradeservice.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TradeServiceApplication {

    @Autowired
    TradeService tradeService;

    public static void main(String[] args) {
        SpringApplication.run(TradeServiceApplication.class, args);
    }
}
