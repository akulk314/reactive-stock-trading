package com.ak.sideprojects.stocktrading.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Quote {


    private static final MathContext MATH_CONTEXT = new MathContext(2);

    public static Quote empty(){
        return new Quote("", 0.0);
    }


    public boolean isEmpty() {
        if (this.price.equals(new BigDecimal(0)) && this.ticker == "") {
            return true;
        }
        return false;
    }
    @Id
    private String id;

    private String ticker;

    private BigDecimal price;

    private Instant instant = Instant.now();

    public Quote(String ticker, BigDecimal price) {
        this.ticker = ticker;
        this.price = price;
    }

    public Quote(String ticker, Double price) {
        this(ticker, new BigDecimal(price, MATH_CONTEXT));
    }

}
