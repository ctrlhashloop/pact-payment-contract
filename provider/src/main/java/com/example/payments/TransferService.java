package com.example.payments;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TransferService {

    public static class InsufficientFundsException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public InsufficientFundsException(String accountId) {
            super("Insufficient funds in account " + accountId);
        }
    }

    public static class TransferNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public TransferNotFoundException(String id) {
            super("Transfer " + id + " not found");
        }
    }

    public static class InvalidTransferException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public InvalidTransferException(String message) {
            super(message);
        }
    }

    private final Map<String, BigDecimal> balances = new ConcurrentHashMap<>();
    private final Map<String, Transfer> transfers = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1000);

    public synchronized Transfer create(TransferRequest request) {
        validate(request);

        BigDecimal available = balances.getOrDefault(request.fromAccount(), BigDecimal.ZERO);
        if (available.compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(request.fromAccount());
        }
        balances.put(request.fromAccount(), available.subtract(request.amount()));
        balances.merge(request.toAccount(), request.amount(), BigDecimal::add);

        Transfer transfer = new Transfer("TRX-" + sequence.incrementAndGet(), "COMPLETED",
                request.fromAccount(), request.toAccount(), request.amount(), request.currency());
        transfers.put(transfer.id(), transfer);
        return transfer;
    }

    public Transfer get(String id) {
        return Optional.ofNullable(transfers.get(id))
                .orElseThrow(() -> new TransferNotFoundException(id));
    }

    private void validate(TransferRequest request) {
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new InvalidTransferException("Amount must be greater than zero");
        }
        if (isBlank(request.fromAccount()) || isBlank(request.toAccount())) {
            throw new InvalidTransferException("Both fromAccount and toAccount are required");
        }
        if (request.fromAccount().equals(request.toAccount())) {
            throw new InvalidTransferException("Cannot transfer to the same account");
        }
        if (isBlank(request.currency())) {
            throw new InvalidTransferException("Currency is required");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // ---- Helpers used only by the Pact provider states and tests (setup) ----

    public synchronized void reset() {
        balances.clear();
        transfers.clear();
        sequence.set(1000);
    }

    public void setBalance(String accountId, BigDecimal balance) {
        balances.put(accountId, balance);
    }

    public void seed(Transfer transfer) {
        transfers.put(transfer.id(), transfer);
    }
}