package com.ak.sideprojects.stocktrading.orderservice.persistence;

import com.ak.sideprojects.stocktrading.common.models.Order;
import com.ak.sideprojects.stocktrading.common.models.OrderStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderReactiveRepository extends ReactiveMongoRepository<Order, String> {

    Flux<Order> findByOrderStatusOrderByLastSeenDateAsc(OrderStatus orderStatus);
}
