package com.example.payments;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransferApiRestAssuredTest {

    @LocalServerPort
    int port;

    @Autowired
    TransferService service;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        service.reset();
        service.setBalance("ACC-001", new BigDecimal("1000.00"));
    }

    private static Map<String, Object> transferBody(String from, String to, Object amount, String currency) {
        Map<String, Object> body = new HashMap<>();
        body.put("fromAccount", from);
        body.put("toAccount", to);
        body.put("amount", amount);
        body.put("currency", currency);
        return body;
    }

    @Test
    void negativeAmount_isRejectedWith400() {
        given().contentType(ContentType.JSON)
                .body(transferBody("ACC-001", "ACC-002", -50.00, "KES"))
        .when().post("/transfers")
        .then().statusCode(400);
    }
}