package com.taghazout.listingservice.domain.model;

import lombok.Value;
import java.math.BigDecimal;

@Value
public class Money {
    BigDecimal amount;
    String currency;
}
