package com.serenitydojo.cashback_rewards.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CashbackCalculator")
class CashbackCalculatorTest {

    private final CashbackCalculator calculator = new CashbackCalculator();

    @Nested
    @DisplayName("Should award cashback of 2% of the purchase amount")
    class AwardTwoPercentCashback {

        @Test
        @DisplayName("The one where a purchase of 100.00 earns 2.00 cashback")
        void purchaseOf100Earns2() {
            BigDecimal cashback = calculator.calculate(new Purchase(new BigDecimal("100.00"), "EUR"));

            assertThat(cashback).isEqualByComparingTo("2.00");
        }
    }

    @Nested
    @DisplayName("Must reject a purchase amount that is zero or negative")
    class RejectNonPositivePurchaseAmount {

        @ParameterizedTest(name = "Purchase {0} is rejected")
        @DisplayName("Zero and negative amounts throw InvalidPurchaseAmountException")
        @ValueSource(strings = {"-10.00", "0.00"})
        void nonPositiveAmountIsRejected(String purchaseAmount) {
            assertThatThrownBy(() -> calculator.calculate(new Purchase(new BigDecimal(purchaseAmount), "EUR")))
                    .isInstanceOf(InvalidPurchaseAmountException.class);
        }
    }

    @Nested
    @DisplayName("Must cap cashback at 50.00 EUR per purchase")
    class CapCashback {

        @Test
        @DisplayName("The one where a purchase of 3000.00 earns 50.00 instead of 60.00")
        void purchaseOf3000IsCappedAt50() {
            BigDecimal cashback = calculator.calculate(new Purchase(new BigDecimal("3000.00"), "EUR"));

            assertThat(cashback).isEqualByComparingTo("50.00");
        }
    }

    @Nested
    @DisplayName("Must accept only EUR purchases")
    class AcceptOnlyEur {

        @ParameterizedTest(name = "Currency {0} is rejected")
        @DisplayName("Currencies other than uppercase EUR throw UnsupportedCurrencyException")
        @ValueSource(strings = {"USD", "eur"})
        void unsupportedCurrencyIsRejected(String currency) {
            assertThatThrownBy(() -> calculator.calculate(new Purchase(new BigDecimal("100.00"), currency)))
                    .isInstanceOf(UnsupportedCurrencyException.class);
        }
    }
}
