package com.example.payments;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final AtomicInteger sequence = new AtomicInteger(1000);

    @PostMapping
    public ResponseEntity<Transfer> create(@RequestBody TransferRequest request) {
        Transfer transfer = new Transfer("TRX-" + sequence.incrementAndGet(), "COMPLETED",
                request.fromAccount(), request.toAccount(), request.amount(), request.currency());
        return ResponseEntity.status(HttpStatus.CREATED).body(transfer);
    }
}