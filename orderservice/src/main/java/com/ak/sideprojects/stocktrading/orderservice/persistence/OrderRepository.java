package com.ak.sideprojects.stocktrading.orderservice.persistence;

import com.ak.sideprojects.stocktrading.common.models.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

}