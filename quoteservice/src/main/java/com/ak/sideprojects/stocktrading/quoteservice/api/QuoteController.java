package com.ak.sideprojects.stocktrading.quoteservice.api;

import com.ak.sideprojects.stocktrading.common.models.Quote;
import com.ak.sideprojects.stocktrading.quoteservice.QuoteGenerator;
import com.ak.sideprojects.stocktrading.quoteservice.persistence.QuoteReactiveRepository;
import com.ak.sideprojects.stocktrading.quoteservice.persistence.QuoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;


@RestController
public class QuoteController {

    Logger logger = LoggerFactory.getLogger(QuoteController.class);

    @Autowired
    QuoteReactiveRepository quoteReactiveRepository;

    @Autowired
    QuoteGenerator quoteGenerator;


    @GetMapping("/quote/{ticker}")
    public Mono<Quote> getQuoteInRange(@PathVariable String ticker,
                                       @RequestParam @NotNull String strMinPrice,
                                       @RequestParam @NotNull String strMaxPrice) {

        BigDecimal minPrice;
        BigDecimal maxPrice;

        try {
            minPrice = new BigDecimal(strMinPrice);
            maxPrice = new BigDecimal(strMaxPrice);
        } catch(NumberFormatException ne) {
            logger.error("Price params are invalid", ne);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Price parameters must be string representations of numbers"));

        }

        return quoteReactiveRepository
                .findFirstByTickerAndPriceBetweenOrderByInstantAsc(ticker, minPrice, maxPrice);
    }

    @DeleteMapping("/quote/{id}")
    public void deleteQuote(@PathVariable @NotNull  String id) {
        quoteReactiveRepository.findById(id)
                .switchIfEmpty(Mono.just(Quote.empty()))
                .doOnNext(quote -> {
                    if (quote.isEmpty()) {
                        logger.info(String.format("Failed to find quote with id %s", id));
                    } else {
                        quoteReactiveRepository.deleteById(quote.getId())
                                .doAfterTerminate( () -> logger.info(String.format("Deleted quote with id %s", id)))
                                .subscribe();
                    }
                })
                .subscribe();
    }
}
