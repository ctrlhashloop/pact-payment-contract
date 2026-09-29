package com.example.wallet;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TransferClient 
{
	private final String baseUrl;
	private final HttpClient http = HttpClient.newHttpClient();
	private final ObjectMapper mapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	
	
	public TransferClient(String baseUrl){
		this.baseUrl = baseUrl;
	}
	
	public TransferResponse createTransfer(TransferRequest request) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(baseUrl + "/transfers"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(request)))
                .build();

        HttpResponse<String> response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return mapper.readValue(response.body(), TransferResponse.class);
        }
        throw new IllegalStateException("Unexpected status " + response.statusCode());
    }
}
