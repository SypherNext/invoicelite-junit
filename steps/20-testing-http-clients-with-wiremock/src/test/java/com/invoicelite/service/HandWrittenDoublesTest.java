package com.invoicelite.service;

import com.invoicelite.invoice.Invoice;
import com.invoicelite.invoice.InvoiceStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.invoicelite.service.InvoiceServiceFixture.MARCH_15;
import static com.invoicelite.service.InvoiceServiceFixture.createMugsAndPens;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The same ideas as Mockito, written by hand: a fake repository, a stub gateway and a recording notifier.
 */
class HandWrittenDoublesTest {

    /** Remembers every call, so the test can check what the service told the customer. */
    static final class RecordingNotifier implements Notifier {
        final List<String> messages = new ArrayList<>();

        @Override
        public void invoiceIssued(Invoice invoice) {
            messages.add("issued " + invoice.number());
        }

        @Override
        public void paymentReceived(Invoice invoice, String paymentReference) {
            messages.add("paid " + invoice.number() + " " + paymentReference);
        }

        @Override
        public void invoiceOverdue(Invoice invoice) {
            messages.add("overdue " + invoice.number());
        }
    }

    private final InMemoryInvoiceRepository repository = new InMemoryInvoiceRepository();
    private final RecordingNotifier notifier = new RecordingNotifier();

    @Test
    void anApprovedPaymentMarksTheInvoicePaid() {
        PaymentGateway alwaysApproves = request -> PaymentResult.approved("pay_123");
        InvoiceService service = InvoiceServiceFixture.service(repository, alwaysApproves, notifier, MARCH_15);
        String number = createMugsAndPens(service).number();
        service.issue(number);

        service.pay(number);

        assertThat(repository.findByNumber(number)).get()
                .extracting(Invoice::status).isEqualTo(InvoiceStatus.PAID);
        assertThat(notifier.messages).containsExactly("issued INV-2026-0001", "paid INV-2026-0001 pay_123");
    }

    @Test
    void aDeclinedPaymentLeavesTheInvoiceIssued() {
        PaymentGateway alwaysDeclines = request -> PaymentResult.declined("card expired");
        InvoiceService service = InvoiceServiceFixture.service(repository, alwaysDeclines, notifier, MARCH_15);
        String number = createMugsAndPens(service).number();
        service.issue(number);

        assertThatThrownBy(() -> service.pay(number))
                .isInstanceOf(PaymentDeclinedException.class)
                .hasMessage("Payment for INV-2026-0001 was declined: card expired");
        assertThat(repository.findByNumber(number)).get()
                .extracting(Invoice::status).isEqualTo(InvoiceStatus.ISSUED);
    }
}
