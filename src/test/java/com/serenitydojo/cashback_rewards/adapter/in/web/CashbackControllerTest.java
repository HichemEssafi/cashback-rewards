package com.serenitydojo.cashback_rewards.adapter.in.web;

import com.serenitydojo.cashback_rewards.application.CalculateCashbackUseCase;
import com.serenitydojo.cashback_rewards.domain.InvalidPurchaseAmountException;
import com.serenitydojo.cashback_rewards.domain.Purchase;
import com.serenitydojo.cashback_rewards.domain.UnsupportedCurrencyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CashbackController.class)
@DisplayName("CashbackController")
class CashbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalculateCashbackUseCase calculateCashback;

    @Nested
    @DisplayName("POST /api/cashback/calculate")
    class Calculate {

        @Test
        @DisplayName("Delegates to the use case and returns its result as 200")
        void delegatesToUseCase() throws Exception {
            given(calculateCashback.calculate(new Purchase(new BigDecimal("100.00"), "EUR"))).willReturn(new BigDecimal("2.00"));

            mockMvc.perform(post("/api/cashback/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"purchaseAmount": 100.00, "currency": "EUR"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {"cashback": 2.00}
                            """));
        }

        @Test
        @DisplayName("Maps InvalidPurchaseAmountException to 400 without leaking internals")
        void mapsInvalidPurchaseAmountTo400() throws Exception {
            given(calculateCashback.calculate(new Purchase(new BigDecimal("-10.00"), "EUR")))
                    .willThrow(new InvalidPurchaseAmountException("Purchase amount must be greater than zero"));

            mockMvc.perform(post("/api/cashback/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"purchaseAmount": -10.00, "currency": "EUR"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json("""
                            {"message": "Purchase amount must be greater than zero"}
                            """))
                    .andExpect(content().string(not(containsString("InvalidPurchaseAmountException"))));
        }

        @Test
        @DisplayName("Maps UnsupportedCurrencyException to 400 without leaking internals")
        void mapsUnsupportedCurrencyTo400() throws Exception {
            given(calculateCashback.calculate(new Purchase(new BigDecimal("100.00"), "USD")))
                    .willThrow(new UnsupportedCurrencyException("Only EUR purchases are supported"));

            mockMvc.perform(post("/api/cashback/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"purchaseAmount": 100.00, "currency": "USD"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json("""
                            {"message": "Only EUR purchases are supported"}
                            """))
                    .andExpect(content().string(not(containsString("UnsupportedCurrencyException"))));
        }

        @ParameterizedTest(name = "Request {0} is rejected naming field {1}")
        @DisplayName("Rejects a missing field with 400 naming the field, without calling the use case")
        @CsvSource(delimiter = '|', value = {
                "{\"currency\": \"EUR\"} | purchaseAmount",
                "{\"purchaseAmount\": 100.00} | currency"
        })
        void rejectsMissingField(String body, String field) throws Exception {
            mockMvc.perform(post("/api/cashback/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString(field)))
                    .andExpect(content().string(not(containsString("MethodArgumentNotValidException"))));

            verifyNoInteractions(calculateCashback);
        }
    }
}
