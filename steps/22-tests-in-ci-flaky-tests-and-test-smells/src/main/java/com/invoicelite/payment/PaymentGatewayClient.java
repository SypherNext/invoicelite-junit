package com.invoicelite.payment;

import com.invoicelite.service.PaymentGateway;
import com.invoicelite.service.PaymentGatewayException;
import com.invoicelite.service.PaymentRequest;
import com.invoicelite.service.PaymentResult;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

/**
 * Talks to the payment provider's HTTP API: POST /v1/charges with a JSON body.
 * 201 means approved, 402 means declined; anything else, a timeout or a broken answer
 * becomes a PaymentGatewayException.
 */
public final class PaymentGatewayClient implements PaymentGateway {

    private static final int CREATED = 201;
    private static final int PAYMENT_REQUIRED = 402;

    record ChargeBody(String invoiceNumber, String amount, String currency) {
    }

    record ChargeAnswer(String id, String status, String reason) {
    }

    private final HttpClient httpClient;
    private final URI chargesUri;
    private final String apiKey;
    private final Duration timeout;
    private final JsonMapper json = JsonMapper.builder().build();

    public PaymentGatewayClient(HttpClient httpClient, URI baseUri, String apiKey, Duration timeout) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.chargesUri = baseUri.resolve("/v1/charges");
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
        this.timeout = Objects.requireNonNull(timeout, "timeout");
    }

    @Override
    public PaymentResult charge(PaymentRequest request) {
        HttpResponse<String> response = send(request);
        int status = response.statusCode();
        if (status == CREATED) {
            return PaymentResult.approved(read(response.body()).id());
        }
        if (status == PAYMENT_REQUIRED) {
            return PaymentResult.declined(read(response.body()).reason());
        }
        throw new PaymentGatewayException("Payment provider answered " + status + " for " + request.invoiceNumber());
    }

    private HttpResponse<String> send(PaymentRequest request) {
        ChargeBody body = new ChargeBody(request.invoiceNumber(),
                request.amount().amount().toPlainString(),
                request.amount().currency().getCurrencyCode());
        HttpRequest httpRequest = HttpRequest.newBuilder(chargesUri)
                .timeout(timeout)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", request.idempotencyKey())
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
                .build();
        try {
            return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new PaymentGatewayException("Payment provider did not answer for " + request.invoiceNumber(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentGatewayException("Interrupted while charging " + request.invoiceNumber(), e);
        }
    }

    private ChargeAnswer read(String body) {
        try {
            return json.readValue(body, ChargeAnswer.class);
        } catch (JacksonException e) {
            throw new PaymentGatewayException("Payment provider sent an answer that is not valid JSON", e);
        }
    }
}
