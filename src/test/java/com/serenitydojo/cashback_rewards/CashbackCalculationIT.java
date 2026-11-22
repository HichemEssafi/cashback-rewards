package com.serenitydojo.cashback_rewards;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Cashback calculation")
class CashbackCalculationIT {

    private static final String CALCULATE_URL = "/api/cashback/calculate";

    @Autowired
    private MockMvc mockMvc;

    private ResultActions calculate(String requestBody) throws Exception {
        return mockMvc.perform(post(CALCULATE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));
    }

    private ResultActions calculate(String purchaseAmount, String currency) throws Exception {
        return calculate("""
                {"purchaseAmount": %s, "currency": "%s"}
                """.formatted(purchaseAmount, currency));
    }

    private void assertCashback(ResultActions result, String expectedCashback) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().json("""
                        {"cashback": %s}
                        """.formatted(expectedCashback)));
    }

    @Nested
    @DisplayName("Should award cashback of 2% of the purchase amount")
    class AwardTwoPercentCashback {

        @Test
        @DisplayName("The one where a customer buys for 100.00 EUR and earns 2.00 EUR cashback")
        void customerBuysFor100EurAndEarns2Eur() throws Exception {
            assertCashback(calculate("100.00", "EUR"), "2.00");
        }
    }

    @Nested
    @DisplayName("Must round cashback down to 2 decimals and never round up")
    class RoundCashbackDown {

        @ParameterizedTest(name = "Purchase {0} EUR earns {1} EUR cashback")
        @DisplayName("Cashback is rounded down")
        @CsvSource({
                "49.99, 0.99",
                "0.50,  0.01",
                "0.49,  0.00"
        })
        void cashbackIsRoundedDown(String purchaseAmount, String expectedCashback) throws Exception {
            assertCashback(calculate(purchaseAmount, "EUR"), expectedCashback);
        }
    }

    @Nested
    @DisplayName("Must round a purchase amount with more than 2 decimals down to 2 decimals before calculating cashback")
    class RoundPurchaseAmountDown {

        @Test
        @DisplayName("The one where a purchase of 49.999 is treated as 49.99 and earns 0.99 (not 1.00)")
        void purchaseOf49_999IsTreatedAs49_99() throws Exception {
            assertCashback(calculate("49.999", "EUR"), "0.99");
        }
    }

    @Nested
    @DisplayName("Must reject a purchase amount that is zero or negative")
    class RejectNonPositivePurchaseAmount {

        @ParameterizedTest(name = "Purchase {0} EUR is rejected")
        @DisplayName("Zero and negative amounts are rejected with 400")
        @CsvSource({
                "-10.00",
                "0.00"
        })
        void nonPositiveAmountIsRejected(String purchaseAmount) throws Exception {
            calculate(purchaseAmount, "EUR").andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("The one where 0.01 is the smallest accepted amount and earns 0.00 cashback")
        void smallestAcceptedAmountIsOneCent() throws Exception {
            assertCashback(calculate("0.01", "EUR"), "0.00");
        }
    }

    @Nested
    @DisplayName("Must cap cashback at 50.00 EUR per purchase")
    class CapCashback {

        @ParameterizedTest(name = "Purchase {0} EUR earns {1} EUR cashback")
        @DisplayName("Cashback never exceeds 50.00")
        @CsvSource({
                "2500.00, 50.00",
                "2500.01, 50.00",
                "2499.99, 49.99",
                "3000.00, 50.00"
        })
        void cashbackIsCapped(String purchaseAmount, String expectedCashback) throws Exception {
            assertCashback(calculate(purchaseAmount, "EUR"), expectedCashback);
        }
    }

    @Nested
    @DisplayName("Must accept only EUR purchases")
    class AcceptOnlyEur {

        @ParameterizedTest(name = "Currency {0} is rejected")
        @DisplayName("Currencies other than uppercase EUR are rejected with 400")
        @CsvSource({
                "USD",
                "eur"
        })
        void unsupportedCurrencyIsRejected(String currency) throws Exception {
            calculate("100.00", currency).andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Must reject a request with a missing purchase amount or a missing currency")
    class RejectMissingFields {

        @ParameterizedTest(name = "Request {0} is rejected naming field {1}")
        @DisplayName("A missing field is rejected with 400 and named in the message")
        @CsvSource(delimiter = '|', value = {
                "{\"currency\": \"EUR\"} | purchaseAmount",
                "{\"purchaseAmount\": 100.00} | currency"
        })
        void missingFieldIsRejected(String body, String field) throws Exception {
            calculate(body)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString(field)));
        }
    }
}
