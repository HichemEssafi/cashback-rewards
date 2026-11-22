package com.serenitydojo.cashback_rewards.adapter.in.web;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CashbackRequest(@NotNull BigDecimal purchaseAmount, @NotNull String currency) {
}
