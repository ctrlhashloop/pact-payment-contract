package com.example.payments;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TransferService {

    public static class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String accountId) {
            super("Insufficient funds in account " + accountId);
        }
    }

    private final Map<String, BigDecimal> balances = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1000);

    public synchronized Transfer create(TransferRequest request) {
        BigDecimal available = balances.getOrDefault(request.fromAccount(), BigDecimal.ZERO);
        if (available.compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(request.fromAccount());
        }
        balances.put(request.fromAccount(), available.subtract(request.amount()));
        balances.merge(request.toAccount(), request.amount(), BigDecimal::add);

        return new Transfer("TRX-" + sequence.incrementAndGet(), "COMPLETED",
                request.fromAccount(), request.toAccount(), request.amount(), request.currency());
    }

    // Used by the Pact provider states to set up test conditions
    public synchronized void reset() {
        balances.clear();
        sequence.set(1000);
    }

    public void setBalance(String accountId, BigDecimal balance) {
        balances.put(accountId, balance);
    }
}