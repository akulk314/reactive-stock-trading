package com.ak.sideprojects.stocktrading.quoteservice.api;

import com.ak.sideprojects.stocktrading.common.models.Quote;
import com.ak.sideprojects.stocktrading.quoteservice.QuoteGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class QuoteHandler {

    @Autowired
    QuoteGenerator quoteGenerator;

    public Mono<ServerResponse> streamQuotes(ServerRequest req) {
        return ok()
                .contentType(APPLICATION_STREAM_JSON)
                .body(this.quoteGenerator.fetchQuoteStream(), Quote.class);
    }



}