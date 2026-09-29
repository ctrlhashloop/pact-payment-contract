package com.example.wallet;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "payments-api", pactVersion = PactSpecVersion.V3)
class TransferClientPactTest {

    private static final Map<String, String> JSON = Map.of("Content-Type", "application/json");

    @Pact(consumer = "wallet-service")
    RequestResponsePact createTransferSucceeds(PactDslWithProvider builder) {
        return builder
                .given("source account ACC-001 has sufficient funds")
                .uponReceiving("a request to create a transfer")
                    .path("/transfers")
                    .method("POST")
                    .headers(JSON)
                    .body(new PactDslJsonBody()
                            .stringValue("fromAccount", "ACC-001")
                            .stringValue("toAccount", "ACC-002")
                            .numberType("amount", 250.00)
                            .stringValue("currency", "KES"))
                .willRespondWith()
                    .status(201)
                    .headers(JSON)
                    .body(new PactDslJsonBody()
                            .stringMatcher("id", "TRX-\\d+", "TRX-1001")
                            .stringValue("status", "COMPLETED")
                            .stringValue("fromAccount", "ACC-001")
                            .stringValue("toAccount", "ACC-002")
                            .numberType("amount", 250.00)
                            .stringValue("currency", "KES"))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createTransferSucceeds")
    void createTransfer_returnsCompletedTransfer(MockServer mockServer) throws Exception {
        TransferClient client = new TransferClient(mockServer.getUrl());

        TransferResponse response = client.createTransfer(
                new TransferRequest("ACC-001", "ACC-002", new BigDecimal("250.00"), "KES"));

        assertEquals("COMPLETED", response.status());
        assertTrue(response.id().startsWith("TRX-"));
    }
    @Pact(consumer = "wallet-service")
    RequestResponsePact createTransferRejectedForInsufficientFunds(PactDslWithProvider builder) {
        return builder
                .given("source account ACC-001 has insufficient funds")
                .uponReceiving("a request to create a transfer that exceeds the balance")
                    .path("/transfers")
                    .method("POST")
                    .headers(JSON)
                    .body(new PactDslJsonBody()
                            .stringValue("fromAccount", "ACC-001")
                            .stringValue("toAccount", "ACC-002")
                            .numberType("amount", 250.00)
                            .stringValue("currency", "KES"))
                .willRespondWith()
                    .status(422)
                    .headers(JSON)
                    .body(new PactDslJsonBody()
                            .stringValue("code", "INSUFFICIENT_FUNDS")
                            .stringType("message", "Insufficient funds in account ACC-001"))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createTransferRejectedForInsufficientFunds")
    void createTransfer_insufficientFunds_isRejected(MockServer mockServer) {
        TransferClient client = new TransferClient(mockServer.getUrl());

        TransferRejectedException ex = assertThrows(TransferRejectedException.class, () ->
                client.createTransfer(new TransferRequest("ACC-001", "ACC-002", new BigDecimal("250.00"), "KES")));

        assertEquals("INSUFFICIENT_FUNDS", ex.getCode());
    }
    @Pact(consumer = "wallet-service")
    RequestResponsePact getExistingTransfer(PactDslWithProvider builder) {
        return builder
                .given("transfer TRX-1001 exists")
                .uponReceiving("a request to get an existing transfer")
                    .path("/transfers/TRX-1001")
                    .method("GET")
                .willRespondWith()
                    .status(200)
                    .headers(JSON)
                    .body(new PactDslJsonBody()
                            .stringValue("id", "TRX-1001")
                            .stringValue("status", "COMPLETED")
                            .stringValue("fromAccount", "ACC-001")
                            .stringValue("toAccount", "ACC-002")
                            .numberType("amount", 250.00)
                            .stringValue("currency", "KES"))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getExistingTransfer")
    void getTransfer_existing_returnsTransfer(MockServer mockServer) throws Exception {
        TransferClient client = new TransferClient(mockServer.getUrl());

        TransferResponse response = client.getTransfer("TRX-1001").orElseThrow();

        assertEquals("TRX-1001", response.id());
        assertEquals("COMPLETED", response.status());
    }

    @Pact(consumer = "wallet-service")
    RequestResponsePact getMissingTransfer(PactDslWithProvider builder) {
        return builder
                .given("transfer TRX-9999 does not exist")
                .uponReceiving("a request to get a transfer that does not exist")
                    .path("/transfers/TRX-9999")
                    .method("GET")
                .willRespondWith()
                    .status(404)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getMissingTransfer")
    void getTransfer_missing_returnsEmpty(MockServer mockServer) throws Exception {
        TransferClient client = new TransferClient(mockServer.getUrl());

        assertTrue(client.getTransfer("TRX-9999").isEmpty());
    }
}