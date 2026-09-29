package com.example.wallet;
import java.math.BigDecimal;


public record TransferResponse(String id, String status, String fromAccount, String toAccount,
        BigDecimal amount, String currency) {

}
