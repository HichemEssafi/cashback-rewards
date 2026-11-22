package com.serenitydojo.cashback_rewards.adapter.in.web;

import com.serenitydojo.cashback_rewards.application.CalculateCashbackUseCase;
import com.serenitydojo.cashback_rewards.domain.Purchase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cashback")
public class CashbackController {

    private final CalculateCashbackUseCase calculateCashback;

    public CashbackController(CalculateCashbackUseCase calculateCashback) {
        this.calculateCashback = calculateCashback;
    }

    @PostMapping("/calculate")
    public CashbackResponse calculate(@Valid @RequestBody CashbackRequest request) {
        Purchase purchase = new Purchase(request.purchaseAmount(), request.currency());
        return new CashbackResponse(calculateCashback.calculate(purchase));
    }
}
