package com.ak.sideprojects.stocktrading.orderservice.service;

import com.ak.sideprojects.stocktrading.common.models.Order;
import com.ak.sideprojects.stocktrading.common.models.OrderStatus;
import com.ak.sideprojects.stocktrading.orderservice.persistence.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 *
    TODO: Need a state machine to go through various phases as order fill process is async
 *
 **/

@Service
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    Logger logger = LoggerFactory.getLogger(OrderService.class);

    public Order upsertOrder(Order order) {

        Optional<Order> optional = order.getId() == null ? Optional.empty() : orderRepository.findById(order.getId());

        if (optional.isPresent()) {
            Order foundOrder = optional.get();
            if(!foundOrder.getOrderStatus().equals(OrderStatus.OPEN)) {
                logger.warn("Attempting to modify a non-open order, returning");
                return foundOrder;
            }

            // Update prices
            if(order.getMaxPrice() != null) {
                foundOrder.setMaxPrice(order.getMaxPrice());
            }

            if(order.getMinPrice() != null) {
                foundOrder.setMinPrice(order.getMinPrice());
            }

            if(order.getFillPrice() != null) {
                foundOrder.setFillPrice(order.getFillPrice());
            }

            // Update status
            if(order.getOrderStatus() != null) {
                foundOrder.setOrderStatus(order.getOrderStatus());
            }

            if(order.getExpiryDate() != null) {
                foundOrder.setExpiryDate(order.getExpiryDate());
            }

            logger.info(String.format("Updated order %s", order));
            return orderRepository.save(foundOrder);
        } else {

            // read only fields for this api
            order.setFillPrice(null);
            order.setOrderStatus(OrderStatus.OPEN);
            logger.info(String.format("Saved order %s", order));
            orderRepository.save(order);
            return order;
        }
    }

}
