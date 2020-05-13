package com.ak.sideprojects.stocktrading.quoteservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@EnableMongoRepositories
@EnableReactiveMongoRepositories
@SpringBootApplication
public class QuoteServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuoteServiceApplication.class, args);
    }
}