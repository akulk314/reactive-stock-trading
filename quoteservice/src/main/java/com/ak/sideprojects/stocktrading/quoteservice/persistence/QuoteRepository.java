package com.ak.sideprojects.stocktrading.quoteservice.persistence;

import com.ak.sideprojects.stocktrading.common.models.Quote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteRepository extends MongoRepository<Quote, String> {
}
