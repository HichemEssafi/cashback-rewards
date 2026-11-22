# Cashback calculation

Status: approved. No open questions.

## Rules

### Rule: Should award cashback of 2% of the purchase amount.

- Example: The one where a customer buys for 100.00 EUR and earns 2.00 EUR cashback.
- Counter-example: The one where a customer buys for 3,000.00 EUR (exact 2% is 60.00) and earns 50.00, because of the cap (see the cap rule).

### Rule: Must round cashback down to 2 decimals and never round up.

| Purchase (EUR) | Exact 2% | Cashback (EUR) |
|---|---|---|
| 49.99 | 0.9998 | 0.99 |
| 0.50 | 0.0100 | 0.01 |
| 0.49 | 0.0098 | 0.00 |

- Counter-example: The one where a very small purchase (0.49) is valid but earns 0.00 cashback.

### Rule: Must round a purchase amount with more than 2 decimals down to 2 decimals before calculating cashback.

- Example: The one where a purchase of 49.999 is treated as 49.99 and earns 0.99 (not 1.00).

### Rule: Must reject a purchase amount that is zero or negative.

- Example: The one where a purchase of -10.00 is rejected as invalid.
- Counter-example: The one where a purchase of 0.00 is rejected, and 0.01 is the smallest accepted amount.

### Rule: Must cap cashback at 50.00 EUR per purchase.

| Purchase (EUR) | Exact 2% | Cashback (EUR) |
|---|---|---|
| 2,500.00 | 50.00 | 50.00 |
| 2,500.01 | 50.0002 | 50.00 |

- Counter-example: The one where a purchase of 2,499.99 (exact 2% is 49.9998) earns 49.99, just under the cap.

### Rule: Must accept only EUR purchases.

The currency is matched exactly; only the uppercase code `EUR` is valid.

- Example: The one where a purchase in EUR is accepted.
- Counter-example: The one where a purchase in USD is rejected as an unsupported currency (400).
- Counter-example: The one where a purchase in lowercase `eur` is rejected as unsupported (400).

### Rule: Must reject a request with a missing purchase amount or a missing currency.

- Example: The one where a request without `purchaseAmount` is rejected with 400.
- Example: The one where a request without `currency` is rejected with 400.
- The error response names the affected field and exposes no internal details.

## Out of scope

- Refunds and returns
- Cashback payout and balances
- Rates that vary by customer, merchant or category
