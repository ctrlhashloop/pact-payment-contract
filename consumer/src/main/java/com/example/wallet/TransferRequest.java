package com.example.wallet;
import java.math.BigDecimal;

public record TransferRequest(String fromAccount, String toAccount, BigDecimal amount, String currency) {

}
