package com.serenitydojo.cashback_rewards.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CashbackCalculator {

    private static final BigDecimal CASHBACK_RATE = new BigDecimal("0.02");
    private static final BigDecimal MAX_CASHBACK = new BigDecimal("50.00");
    private static final String SUPPORTED_CURRENCY = "EUR";

    public BigDecimal calculate(Purchase purchase) {
        if (!SUPPORTED_CURRENCY.equals(purchase.currency())) {
            throw new UnsupportedCurrencyException("Only EUR purchases are supported");
        }
        if (purchase.amount().signum() <= 0) {
            throw new InvalidPurchaseAmountException("Purchase amount must be greater than zero");
        }
        BigDecimal cashback = purchase.amount().multiply(CASHBACK_RATE).setScale(2, RoundingMode.DOWN);
        return cashback.min(MAX_CASHBACK);
    }
}
