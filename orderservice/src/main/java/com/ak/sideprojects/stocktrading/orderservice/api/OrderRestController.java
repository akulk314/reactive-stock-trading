package com.ak.sideprojects.stocktrading.orderservice.api;

import com.ak.sideprojects.stocktrading.common.models.Order;
import com.ak.sideprojects.stocktrading.common.models.OrderStatus;
import com.ak.sideprojects.stocktrading.orderservice.OrderGenerator;
import com.ak.sideprojects.stocktrading.orderservice.persistence.OrderReactiveRepository;
import com.ak.sideprojects.stocktrading.orderservice.persistence.OrderRepository;
import com.ak.sideprojects.stocktrading.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Validated
@RestController
public class OrderRestController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderReactiveRepository orderReactiveRepository;

    @Autowired
    OrderService orderService;

    Logger logger = LoggerFactory.getLogger(OrderRestController.class);

    @GetMapping("/orders")
    public Flux<Order> streamOrders() {
        return orderReactiveRepository.findAll();
    }

    @GetMapping("/order/batch")
    public Flux<Order> getOrderBatch(@RequestParam @NotNull Long batchSize) {
        return orderReactiveRepository.findByOrderStatusOrderByLastSeenDateAsc(OrderStatus.OPEN)
                .limitRequest(batchSize)
                .doOnNext(order -> logger.info(String.format("Found order %s", order)));
    }

    @GetMapping("/order/{id}")
    public Order findOrderById(@PathVariable @NotBlank String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Order with id %s not found", id)));
    }

    @PostMapping("/order/")
    public Order createOrder(@RequestBody @Valid Order order) {
        return orderService.upsertOrder(order);
    }

    @PutMapping("/order/")
    public Order updateOrder(@RequestBody @Valid Order order) {
        return orderService.upsertOrder(order);
    }

    @DeleteMapping("/order/{id}")
    public void deleteOrder(@PathVariable @NotBlank String id) {
        Order foundOrder = findOrderById(id);
        orderRepository.delete(foundOrder);
    }

}
