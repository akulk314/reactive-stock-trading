package com.ak.sideprojects.stocktrading.orderservice;

import com.ak.sideprojects.stocktrading.common.models.Order;
import com.ak.sideprojects.stocktrading.orderservice.persistence.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class OrderGenerator {

    private final MathContext mathContext = new MathContext(2);

    private final Random random = new Random();

    private final List<Order> orderList = new ArrayList<>();

    private final Flux<Order> orderStream;

    Logger logger = LoggerFactory.getLogger(OrderGenerator.class);

    @Value("${orderservice.generator.persist}")
    private boolean doPersist;

    @Autowired
    OrderRepository orderRepository;

    /**
     * Bootstraps the generator with tickers and initial orderList
     */
    public OrderGenerator() {
        initializeOrders();
        this.orderStream = getOrderStream();
    }

    public Flux<Order> fetchOrderStream() {
        return orderStream;
    }

    /**
     * Subscribe to the quote stream and persist it
     */
    @PostConstruct
    public void persistOrders() {

        if(!doPersist) {
            return;
        }

        this.orderStream
                .subscribe(order -> {
                    Order saved = orderRepository.save(order);
                    logger.info(String.format("Saved order %s", saved));
                });
    }

    private void initializeOrders() {
        this.orderList.add(new Order("AMZN", new BigDecimal(2334.31, mathContext), new BigDecimal(2359.12, mathContext)));
        this.orderList.add(new Order("AAPL", new BigDecimal(297.9, mathContext), new BigDecimal(323.14, mathContext)));
        this.orderList.add(new Order("BAC", new BigDecimal(21.02, mathContext), new BigDecimal(24.7, mathContext)));
    }


    private Flux<Order> getOrderStream() {
        return Flux.interval(Duration.ofSeconds(10))
                .onBackpressureDrop()
                .map(this::generateOrders)
                .flatMapIterable(orders -> orders)
                .share();
    }

    private List<Order> generateOrders(long i) {
        Instant instant = Instant.now();
        return orderList.stream()
                .map(baseOrder -> {
                    BigDecimal priceChange = baseOrder.getMinPrice()
                            .multiply(new BigDecimal(0.05 * this.random.nextDouble()), this.mathContext);

                    Order result = new Order(baseOrder.getTicker(), baseOrder.getMinPrice().add(priceChange), baseOrder.getMaxPrice().add(priceChange));
                    return result;
                })
                .collect(Collectors.toList());
    }
}

