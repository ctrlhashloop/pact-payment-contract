package com.example.payments;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@Provider("payments-api")
@PactFolder("../consumer/target/pacts")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentsApiPactVerificationTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setTarget(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

//    @State("source account ACC-001 has sufficient funds")
//    void sufficientFunds() {
//        // Nothing to set up yet: the API has no balance logic.
//        // This method must still exist so the state is recognized.
//    }
    @Autowired
    TransferService service;

    @State("source account ACC-001 has sufficient funds")
    void sufficientFunds() {
        service.reset();
        service.setBalance("ACC-001", new BigDecimal("10000.00"));
    }

    @State("source account ACC-001 has insufficient funds")
    void insufficientFunds() {
        service.reset();
        service.setBalance("ACC-001", new BigDecimal("10.00"));
    }
    
    @State("transfer TRX-1001 exists")
    void transferExists() {
        service.reset();
        service.seed(new Transfer("TRX-1001", "COMPLETED", "ACC-001", "ACC-002",
                new BigDecimal("250.00"), "KES"));
    }

    @State("transfer TRX-9999 does not exist")
    void transferMissing() {
        service.reset();
    }
}