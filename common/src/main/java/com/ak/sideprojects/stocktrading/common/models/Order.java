package com.ak.sideprojects.stocktrading.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Order {

    @Id
    private String id;

    @NotBlank @NonNull
    private String ticker;

    @Positive @NonNull
    private BigDecimal minPrice;

    @Positive @NonNull
    private BigDecimal maxPrice;

    private BigDecimal fillPrice;

    @NotNull
    private OrderType orderType = OrderType.BUY; // TODO: Handle sell case

    private OrderStatus orderStatus = OrderStatus.OPEN;

    @Future @NonNull
    private Date expiryDate;

    private Date lastSeenDate = Date.from(Instant.now());

    @Version
    private Long version; // Spring Data Optimistic Locking

    public boolean isExpired() {
        if(Date.from(Instant.now()).after(expiryDate)) {
            return true;
        }
        return false;
    }

    @PersistenceConstructor
    public Order(String ticker, BigDecimal minPrice, BigDecimal maxPrice) {
        this.ticker = ticker;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        this.expiryDate = cal.getTime();
    }

}
