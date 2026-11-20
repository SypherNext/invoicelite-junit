package com.invoicelite.payment;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.invoicelite.money.Money;
import com.invoicelite.service.PaymentGatewayException;
import com.invoicelite.service.PaymentRequest;
import com.invoicelite.service.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentGatewayClientTest {

    @RegisterExtension
    static WireMockExtension provider = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private static final PaymentRequest REQUEST =
            new PaymentRequest("INV-2026-0001", Money.of("74.73", "EUR"), "idem-ba419d350dfe8af7");

    private PaymentGatewayClient client;

    @BeforeEach
    void createClient() {
        client = clientWithTimeout(Duration.ofSeconds(5));
    }

    private static PaymentGatewayClient clientWithTimeout(Duration timeout) {
        return new PaymentGatewayClient(HttpClient.newHttpClient(), URI.create(provider.baseUrl()), "test-key", timeout);
    }

    @Test
    void anApprovedChargeReturnsTheProvidersId() {
        provider.stubFor(post("/v1/charges").willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                        {"id": "ch_8f2k", "status": "approved"}""")));

        PaymentResult result = client.charge(REQUEST);

        assertThat(result).isEqualTo(PaymentResult.approved("ch_8f2k"));
    }

    @Test
    void sendsTheRequestTheProviderExpects() {
        provider.stubFor(post("/v1/charges").willReturn(aResponse().withStatus(201).withBody("""
                {"id": "ch_8f2k", "status": "approved"}""")));

        client.charge(REQUEST);

        provider.verify(postRequestedFor(urlEqualTo("/v1/charges"))
                .withHeader("Authorization", equalTo("Bearer test-key"))
                .withHeader("Idempotency-Key", equalTo("idem-ba419d350dfe8af7"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {"invoiceNumber": "INV-2026-0001", "amount": "74.73", "currency": "EUR"}""")));
    }

    @Test
    void aPartPaymentSendsOnlyThatAmount() {
        provider.stubFor(post("/v1/charges").willReturn(aResponse().withStatus(201).withBody("""
                {"id": "ch_9a1c", "status": "approved"}""")));

        client.charge(new PaymentRequest("INV-2026-0001", Money.of("30.00", "EUR"), "idem-1"));

        provider.verify(postRequestedFor(urlEqualTo("/v1/charges"))
                .withRequestBody(equalToJson("""
                        {"invoiceNumber": "INV-2026-0001", "amount": "30.00", "currency": "EUR"}""")));
    }

    @Test
    void aDeclinedChargeCarriesTheReason() {
        provider.stubFor(post("/v1/charges").willReturn(aResponse().withStatus(402).withBody("""
                {"status": "declined", "reason": "card_expired"}""")));

        assertThat(client.charge(REQUEST)).isEqualTo(PaymentResult.declined("card_expired"));
    }

    @Test
    void aServerErrorBecomesAGatewayException() {
        provider.stubFor(post("/v1/charges").willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> client.charge(REQUEST))
                .isInstanceOf(PaymentGatewayException.class)
                .hasMessage("Payment provider answered 503 for INV-2026-0001");
    }

    @Test
    void aSlowProviderTimesOut() {
        provider.stubFor(post("/v1/charges").willReturn(aResponse().withStatus(201).withFixedDelay(1_000)));
        PaymentGatewayClient impatientClient = clientWithTimeout(Duration.ofMillis(200));

        assertThatThrownBy(() -> impatientClient.charge(REQUEST))
                .isInstanceOf(PaymentGatewayException.class)
                .hasMessage("Payment provider did not answer for INV-2026-0001")
                .hasCauseInstanceOf(HttpTimeoutException.class);
    }

    @Test
    void aDroppedConnectionBecomesAGatewayException() {
        provider.stubFor(post("/v1/charges").willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertThatThrownBy(() -> client.charge(REQUEST))
                .isInstanceOf(PaymentGatewayException.class)
                .hasMessage("Payment provider did not answer for INV-2026-0001");
    }

    @Test
    void anAnswerThatIsNotJsonBecomesAGatewayException() {
        provider.stubFor(post("/v1/charges").willReturn(aResponse().withStatus(201).withBody("<html>Bad gateway</html>")));

        assertThatThrownBy(() -> client.charge(REQUEST))
                .isInstanceOf(PaymentGatewayException.class)
                .hasMessage("Payment provider sent an answer that is not valid JSON");
    }
}
