package com.ak.sideprojects.stocktrading.tradeservice.apiclient;

import com.ak.sideprojects.stocktrading.common.models.Quote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

@Component
public class QuoteServiceClient {

    // quote service configs
    @Value("${quoteservice.scheme}") private String qsScheme;
    @Value("${quoteservice.host}") private String qsHost;
    @Value("${quoteservice.port}") private String qsPort;
    @Value("${quoteservice.path}") private String qsRootPath;

    // quote feed service configs
    @Value("${quotefeedservice.scheme}") private String qfsScheme;
    @Value("${quotefeedservice.host}") private String qfsHost;
    @Value("${quotefeedservice.port}") private String qfsPort;
    @Value("${quotefeedservice.path}") private String qfsRootPath;

    private Logger logger = LoggerFactory.getLogger(QuoteServiceClient.class);

    /**
     * Find the earliest matching quote for the specified ticker, that is between
     * minPrice and maxPrice (both inclusive).
     *
     * @return Mono representing the matching quote.
     */
    public Mono<Quote> findEarliestMatching(String ticker, BigDecimal minPrice, BigDecimal maxPrice) {
          logger.info(String.format("Calling quote API with %s %s %s", ticker, minPrice, maxPrice));
          return WebClient.builder().build()
                  .get()
                  .uri(uriBuilder -> uriBuilder
                    .scheme(qsScheme).host(qsHost).port(qsPort)
                    .path(qsRootPath).path("/").path(ticker)
                    .queryParam("strMinPrice", minPrice.toString())
                    .queryParam( "strMaxPrice", maxPrice.toString())
                    .build())
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .retrieve()
                .bodyToMono(Quote.class)
                .switchIfEmpty(Mono.just(Quote.empty()))
                .doOnError(error -> logger.error(String.format("An error occurred when fetching quote %s", error)))
                .onErrorReturn(Quote.empty());
    }

    /**
     * Delete the specified quote
     * @param quote
     * @return Mono representing the deleted quote
     */
    public void delete(Quote quote) {
         WebClient.builder().build()
                .delete()
                .uri(uriBuilder -> uriBuilder
                    .scheme(qsScheme).host(qsHost).port(qsPort)
                    .path(qsRootPath).path("/").path(quote.getId())
                    .build())
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .retrieve()
                .bodyToMono(Quote.class)
                .doOnError(error -> logger.error(String.format("An error occurred while deleted quote %s", error)))
                .subscribe(deletedQuote -> logger.info(String.format("Deleted quote ", deletedQuote)));
    }

    /**
     * Consume quote feed test if connection remains open
     * @return Mono representing the deleted quote
     */
    public Flux<Quote> consumeFeed() {
        return WebClient.builder().build()
                .get()
                .uri(uriBuilder ->  uriBuilder
                    .scheme(qfsScheme).host(qfsHost).port(qfsPort).path(qfsRootPath)
                    .build())
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .retrieve()
                .bodyToFlux(Quote.class);
    }
}
