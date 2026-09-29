package com.example.wallet;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

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

        switch (response.statusCode()) {
	        case 201:
	            return mapper.readValue(response.body(), TransferResponse.class);
	        case 422:
	            ApiError error = mapper.readValue(response.body(), ApiError.class);
	            throw new TransferRejectedException(error.code(), error.message());
	        default:
	            throw new IllegalStateException("Unexpected status " + response.statusCode());
        }
    }
    public Optional<TransferResponse> getTransfer(String id) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(baseUrl + "/transfers/" + id))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        switch (response.statusCode()) {
            case 200:
                return Optional.of(mapper.readValue(response.body(), TransferResponse.class));
            case 404:
                return Optional.empty();
            default:
                throw new IllegalStateException("Unexpected status " + response.statusCode());
        }
    }
}
