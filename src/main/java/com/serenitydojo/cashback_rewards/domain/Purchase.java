package com.serenitydojo.cashback_rewards.domain;

import java.math.BigDecimal;

public record Purchase(BigDecimal amount, String currency) {
}
