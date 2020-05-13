package com.ak.sideprojects.stocktrading.quoteservice.persistence;

import com.ak.sideprojects.stocktrading.common.models.Quote;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Repository
public interface QuoteReactiveRepository extends ReactiveMongoRepository<Quote, String> {

    /**
     * Find the oldest quote for the specified ticker and in the specified price range.
     *
     * SpringData magic auto-generates the query :)
     *
     * @param ticker ticker to search for
     * @param minPrice price range lower bound
     * @param maxPrice price range upper bound
     * @return Mono<Quote>
     */
    Mono<Quote> findFirstByTickerAndPriceBetweenOrderByInstantAsc(String ticker, BigDecimal minPrice, BigDecimal maxPrice);
}
