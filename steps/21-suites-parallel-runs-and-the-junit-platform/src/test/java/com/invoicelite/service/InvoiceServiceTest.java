package com.invoicelite.service;

import com.invoicelite.invoice.Invoice;
import com.invoicelite.invoice.InvoiceStatus;
import com.invoicelite.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.invoicelite.service.InvoiceServiceFixture.MARCH_15;
import static com.invoicelite.service.InvoiceServiceFixture.createMugsAndPens;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private PaymentGateway gateway;

    @Mock
    private Notifier notifier;

    private final InMemoryInvoiceRepository repository = new InMemoryInvoiceRepository();
    private InvoiceService service;

    @BeforeEach
    void createService() {
        service = InvoiceServiceFixture.service(repository, gateway, notifier, MARCH_15);
    }

    @Test
    void createsADraftWithTheNextNumber() {
        Invoice draft = createMugsAndPens(service);

        assertThat(draft.number()).isEqualTo("INV-2026-0001");
        assertThat(draft.customer()).isEqualTo("Acme GmbH");
        assertThat(draft.status()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(repository.findByNumber("INV-2026-0001")).hasValue(draft);
    }

    @Test
    void issuingTellsTheCustomer() {
        Invoice draft = createMugsAndPens(service);

        Invoice issued = service.issue(draft.number());

        assertThat(issued.status()).isEqualTo(InvoiceStatus.ISSUED);
        verify(notifier).invoiceIssued(issued);
    }

    @Test
    void payingChargesTheTotalAndTellsTheCustomer() {
        Invoice invoice = createMugsAndPens(service);
        service.issue(invoice.number());
        String firstKey = InvoiceServiceFixture.keys().next();
        PaymentRequest expectedCharge = new PaymentRequest("INV-2026-0001", Money.of("74.73", "EUR"), firstKey);
        when(gateway.charge(expectedCharge)).thenReturn(PaymentResult.approved("pay_123"));

        PaymentResult result = service.pay(invoice.number());

        assertThat(result.reference()).isEqualTo("pay_123");
        assertThat(repository.findByNumber(invoice.number())).get()
                .extracting(Invoice::status).isEqualTo(InvoiceStatus.PAID);
        verify(notifier).paymentReceived(invoice, "pay_123");
    }

    @Test
    void aDraftIsNeverCharged() {
        Invoice draft = createMugsAndPens(service);

        assertThatThrownBy(() -> service.pay(draft.number()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invoice INV-2026-0001 is DRAFT and cannot be paid");
        verifyNoInteractions(gateway, notifier);
    }

    @Test
    void anUnknownNumberIsReported() {
        assertThatThrownBy(() -> service.issue("INV-2026-0099"))
                .isInstanceOf(InvoiceNotFoundException.class)
                .hasMessage("Invoice INV-2026-0099 was not found");
    }
}
