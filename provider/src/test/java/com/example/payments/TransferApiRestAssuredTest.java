package com.example.payments;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

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

//    @Test
//    void negativeAmount_isRejectedWith400() {
//        given().contentType(ContentType.JSON)
//                .body(transferBody("ACC-001", "ACC-002", -50.00, "KES"))
//        .when().post("/transfers")
//        .then().statusCode(400);
//    }
    static Stream<Arguments> invalidTransfers() {
        return Stream.of(
                Arguments.of("negative amount", transferBody("ACC-001", "ACC-002", -50.00, "KES")),
                Arguments.of("zero amount", transferBody("ACC-001", "ACC-002", 0, "KES")),
                Arguments.of("missing amount", transferBody("ACC-001", "ACC-002", null, "KES")),
                Arguments.of("missing source account", transferBody(null, "ACC-002", 10.00, "KES")),
                Arguments.of("blank destination account", transferBody("ACC-001", " ", 10.00, "KES")),
                Arguments.of("same source and destination", transferBody("ACC-001", "ACC-001", 10.00, "KES")),
                Arguments.of("missing currency", transferBody("ACC-001", "ACC-002", 10.00, null))
        );
    }

    @ParameterizedTest(name = "{0} is rejected with 400")
    @MethodSource("invalidTransfers")
    void invalidTransfer_isRejectedWith400(String scenario, Map<String, Object> body) {
        given().contentType(ContentType.JSON)
                .body(body)
        .when().post("/transfers")
        .then()
                .statusCode(400)
                .body("code", equalTo("INVALID_TRANSFER"));
    }
}