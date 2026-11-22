package com.serenitydojo.cashback_rewards.application;

import com.serenitydojo.cashback_rewards.domain.CashbackCalculator;
import com.serenitydojo.cashback_rewards.domain.Purchase;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CalculateCashbackUseCase {

    private final CashbackCalculator calculator = new CashbackCalculator();

    public BigDecimal calculate(Purchase purchase) {
        return calculator.calculate(purchase);
    }
}
