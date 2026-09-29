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
}