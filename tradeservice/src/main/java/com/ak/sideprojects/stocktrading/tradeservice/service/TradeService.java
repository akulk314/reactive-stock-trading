package com.ak.sideprojects.stocktrading.tradeservice.service;


import com.ak.sideprojects.stocktrading.common.models.Order;
import com.ak.sideprojects.stocktrading.common.models.OrderStatus;
import com.ak.sideprojects.stocktrading.tradeservice.apiclient.OrderServiceClient;
import com.ak.sideprojects.stocktrading.tradeservice.apiclient.QuoteServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

@Component
public class TradeService {

    @Autowired
    OrderServiceClient orderServiceClient;

    @Autowired
    QuoteServiceClient quoteServiceClient;

    Logger logger = LoggerFactory.getLogger(TradeService.class);

    @Value("${tradeservice.order.batchsize}")
    private Long orderBatchSize;



    /**
     * Fetch a batch of orders to execute and for each order in the batch:
     *
     * Wait for a quote where the price is in range. If such a quote is found,
     * delete the quote and mark the order as complete.
     *
     * Otherwise, if the order has not expired, update its last seen time so it doesn't block new
     * orders.
     *
     */
    @Scheduled(fixedRate = 10000, initialDelay = 5000)
    public void execute() {
        logger.info("Executing trade service");
        orderServiceClient.fetchBatchFifoOrder(orderBatchSize)
                .subscribe(fetchedOrder -> {
                            logger.info(String.format("Processing order %s", fetchedOrder));
                            quoteServiceClient.findEarliestMatching(fetchedOrder.getTicker(), fetchedOrder.getMinPrice(), fetchedOrder.getMaxPrice())
                                .subscribe(quote -> {
                                            logger.info(String.format("Found matching quote %s", quote));
                                            fetchedOrder.setLastSeenDate(Date.from(Instant.now()));
                                            if (!quote.isEmpty()) {
                                                quoteServiceClient.delete(quote);
                                                fetchedOrder.setFillPrice(quote.getPrice());
                                                fetchedOrder.setOrderStatus(OrderStatus.FILLED);
                                            } else {

                                                if (fetchedOrder.isExpired()) {
                                                    fetchedOrder.setOrderStatus(OrderStatus.EXPIRED);
                                                    logger.info(String.format("Expired order %s", fetchedOrder));
                                                }
                                            }

                                            // update the expired date so API doesn't reject it
                                            Calendar cal = Calendar.getInstance();
                                            cal.add(Calendar.MONTH, 1);
                                            fetchedOrder.setExpiryDate(cal.getTime());

                                            orderServiceClient.updateOrder(fetchedOrder)
                                                .subscribe(updatedOrder -> logger.info(String.format("Updated order %s", updatedOrder)));
                                            },
                                        error -> {
                                            logger.error(String.format("Failed to find earliest matching quote %s", error));
                                        },
                                        () -> { logger.info("Fetched matching quote"); }
                                );
                            },
                            error -> {
                                logger.error(String.format("Failed to fetch order %s", error));
                            },
                            () -> {
                                logger.info("Processing complete");
                            }
                );

    }

//    @Scheduled(fixedRate = 20000, initialDelay = 5000)
//    public void testExecute(){
////        Mono.just(1)
////                .subscribe(intValue -> logger.info(String.format("Received %s", intValue)));
//
//        orderServiceClient.fetchBatchFifoOrder(orderBatchSize)
////            quoteServiceClient.findEarliestMatching(order.getTicker(), order.getMinPrice(), order.getMaxPrice())
////        quoteServiceClient.findEarliestMatching("BAC", new BigDecimal("21.0"), new BigDecimal("24"))
//                .subscribe(order -> {
//                            logger.info(String.format("Received order %s", order));
//                            quoteServiceClient.findEarliestMatching(order.getTicker(), order.getMinPrice(), order.getMaxPrice())
////                            quoteServiceClient.findEarliestMatching(order.getTicker(), new BigDecimal("21.0"), new BigDecimal("99.9"))
//                                    .subscribe(quote -> logger.info(String.format("Found quote %s", quote)));
//                        },
//                        error -> {
//                            logger.error("Error");
//                        },
//                        () -> { logger.info("Complete"); }
//                );
//    }

}


