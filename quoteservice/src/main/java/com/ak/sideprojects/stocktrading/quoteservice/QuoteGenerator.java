package com.ak.sideprojects.stocktrading.quoteservice;

import com.ak.sideprojects.stocktrading.common.models.Quote;
import com.ak.sideprojects.stocktrading.quoteservice.persistence.QuoteRepository;
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
public class QuoteGenerator {

    private final MathContext mathContext = new MathContext(2);

    private final Random random = new Random();

    private final List<Quote> quoteList = new ArrayList<>();

    private final Flux<Quote> quoteStream;

    Logger logger = LoggerFactory.getLogger(QuoteGenerator.class);

    @Value("${quoteservice.generator.persist}")
    private boolean doPersist;

    @Autowired
    QuoteRepository quoteRepository;

    /**
     * Bootstraps the generator with tickers and initial quoteList
     */
    public QuoteGenerator() {
        initializeQuotes();
        this.quoteStream = getQuoteStream();
    }

    public Flux<Quote> fetchQuoteStream() {
        return quoteStream;
    }

    /**
     * Subscribe to the quote stream and persist it
     */
    @PostConstruct
    public void persistQuotes() {

        if(!this.doPersist) {
            logger.info("Persist is disabled");
            return;
        }

        this.quoteStream
                .subscribe(quote -> {
                    Quote saved = quoteRepository.save(quote);
                    logger.info(String.format("Saved quote %s", saved));
                });
    }

    private void initializeQuotes() {
        this.quoteList.add(new Quote("AMZN", 2359.12));
        this.quoteList.add(new Quote("AAPL", 301.26));
        this.quoteList.add(new Quote("BAC", 22.04));
    }


    private Flux<Quote> getQuoteStream() {
        return Flux.interval(Duration.ofSeconds(10))
                .onBackpressureDrop()
                .map(this::generateQuotes)
                .flatMapIterable(quotes -> quotes)
                .share();
    }

    private List<Quote> generateQuotes(long i) {
        Instant instant = Instant.now();
        return quoteList.stream()
                .map(baseQuote -> {
                    BigDecimal priceChange = baseQuote.getPrice()
                            .multiply(new BigDecimal(0.05 * this.random.nextDouble()), this.mathContext);

                    Quote result = new Quote(baseQuote.getTicker(), baseQuote.getPrice().add(priceChange));
                    result.setInstant(instant);
                    return result;
                })
                .collect(Collectors.toList());
    }
}