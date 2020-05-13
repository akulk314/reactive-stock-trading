package com.ak.sideprojects.stocktrading.tradeservice.apiclient;

import com.ak.sideprojects.stocktrading.common.models.Order;
import com.ak.sideprojects.stocktrading.common.models.Quote;
import com.ak.sideprojects.stocktrading.tradeservice.service.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderServiceClient {

    // order service configs
    @Value("${orderservice.scheme}") private String osScheme;
    @Value("${orderservice.host}") private String osHost;
    @Value("${orderservice.port}") private String osPort;
    @Value("${orderservice.path}") private String osRootPath;

    // order feed service configs
    @Value("${orderfeedservice.scheme}") private String ofsScheme;
    @Value("${orderfeedservice.host}") private String ofsHost;
    @Value("${orderfeedservice.port}") private String ofsPort;
    @Value("${orderfeedservice.path}") private String ofsRootPath;


    private Logger logger = LoggerFactory.getLogger(OrderServiceClient.class);


    /**
     * Fetch "batchsize" number of orders from the order service, in FIFO order.
     * @param batchSize
     * @return list of orders
     */
    public Flux<Order> fetchBatchFifoOrder(long batchSize) {
        return WebClient.builder().build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme(osScheme).host(osHost).port(osPort).path(osRootPath).path("/batch")
                        .queryParam("batchSize", batchSize)
                        .build())
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .retrieve()
                .bodyToFlux(Order.class)
                .doOnError(error -> logger.error(String.format("An error occurred while fetching Order batch %s", error)));
    }

    /**
     * Update the specified order
     * @param order
     */
    public Mono<Order> updateOrder(Order order) {
        return WebClient.builder().build()
                .put()
                .uri(uriBuilder -> uriBuilder
                        .scheme(osScheme).host(osHost).port(osPort).path(osRootPath).path("/")
                        .build())
                .body(Mono.just(order), Order.class)
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .retrieve()
                .bodyToMono(Order.class)
                .doOnError(error -> logger.error("An error occurred while updating order %s", error));

    }


}
