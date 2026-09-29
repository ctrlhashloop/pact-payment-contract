package com.example.payments;

import java.math.BigDecimal;

public record Transfer(String id, String status, String fromAccount, String toAccount,
                       BigDecimal amount, String currency) {
}