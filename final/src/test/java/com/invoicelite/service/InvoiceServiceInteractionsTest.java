package com.invoicelite.service;

import com.invoicelite.invoice.Invoice;
import com.invoicelite.invoice.InvoiceStatus;
import com.invoicelite.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static com.invoicelite.service.InvoiceServiceFixture.MARCH_15;
import static com.invoicelite.service.InvoiceServiceFixture.createMugsAndPens;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceInteractionsTest {

    private static final Clock APRIL_20 = Clock.fixed(Instant.parse("2026-04-20T09:00:00Z"), ZoneId.of("Europe/Berlin"));

    @Mock
    private PaymentGateway gateway;

    @Mock
    private Notifier notifier;

    @Captor
    private ArgumentCaptor<Invoice> invoiceCaptor;

    private final InMemoryInvoiceRepository repository = new InMemoryInvoiceRepository();
    private InvoiceService service;

    @BeforeEach
    void createService() {
        service = InvoiceServiceFixture.service(repository, gateway, notifier, MARCH_15);
    }

    @Test
    void chargesBeforeTellingTheCustomer() {
        String number = issuedInvoice();
        when(gateway.charge(any(PaymentRequest.class))).thenReturn(PaymentResult.approved("pay_123"));

        service.pay(number);

        InOrder inOrder = inOrder(gateway, notifier);
        inOrder.verify(gateway).charge(any(PaymentRequest.class));
        inOrder.verify(notifier).paymentReceived(any(Invoice.class), anyString());
    }

    @Test
    void aGatewayFailureLeavesTheInvoiceUnpaid() {
        String number = issuedInvoice();
        when(gateway.charge(any())).thenThrow(new PaymentGatewayException("Payment provider timed out"));

        assertThatThrownBy(() -> service.pay(number))
                .isInstanceOf(PaymentGatewayException.class);

        assertThat(repository.findByNumber(number)).get()
                .extracting(Invoice::status).isEqualTo(InvoiceStatus.ISSUED);
        verify(notifier, never()).paymentReceived(any(), any());
    }

    @Test
    void theReferenceComesFromTheGateway() {
        String number = issuedInvoice();
        when(gateway.charge(any())).thenAnswer(call -> {
            PaymentRequest request = call.getArgument(0);
            return PaymentResult.approved("pay_" + request.invoiceNumber());
        });

        PaymentResult result = service.pay(number);

        assertThat(result.reference()).isEqualTo("pay_INV-2026-0001");
    }

    @Test
    void chargesTheTotalIncludingTax() {
        String number = issuedInvoice();
        given(gateway.charge(argThat(request -> request.amount().equals(Money.of("74.73", "EUR")))))
                .willReturn(PaymentResult.approved("pay_123"));

        service.pay(number);

        then(notifier).should().paymentReceived(any(Invoice.class), argThat(reference -> reference.startsWith("pay_")));
    }

    @Test
    void marksEveryLateInvoiceOverdueAndTellsEachCustomer() {
        String first = issuedInvoice();
        String second = issuedInvoice();
        createMugsAndPens(service);
        InvoiceService fiveWeeksLater = InvoiceServiceFixture.service(repository, gateway, notifier, APRIL_20);

        int marked = fiveWeeksLater.markOverdueInvoices();

        assertThat(marked).isEqualTo(2);
        verify(notifier, times(2)).invoiceOverdue(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getAllValues())
                .extracting(Invoice::number)
                .containsExactlyInAnyOrder(first, second);
        assertThat(invoiceCaptor.getAllValues())
                .extracting(Invoice::status)
                .containsOnly(InvoiceStatus.OVERDUE);
    }

    @Test
    void savesEveryInvoiceItMarksOverdue() {
        InMemoryInvoiceRepository spiedRepository = spy(new InMemoryInvoiceRepository());
        Invoice invoice = createMugsAndPens(InvoiceServiceFixture.service(spiedRepository, gateway, notifier, MARCH_15));
        InvoiceServiceFixture.service(spiedRepository, gateway, notifier, MARCH_15).issue(invoice.number());
        clearInvocations(spiedRepository);

        InvoiceServiceFixture.service(spiedRepository, gateway, notifier, APRIL_20).markOverdueInvoices();

        verify(spiedRepository).save(invoice);
    }

    @Test
    void savesThePaidInvoice() {
        InMemoryInvoiceRepository spiedRepository = spy(new InMemoryInvoiceRepository());
        InvoiceService spiedService = InvoiceServiceFixture.service(spiedRepository, gateway, notifier, MARCH_15);
        Invoice invoice = createMugsAndPens(spiedService);
        spiedService.issue(invoice.number());
        when(gateway.charge(any())).thenReturn(PaymentResult.approved("pay_123"));
        clearInvocations(spiedRepository);

        spiedService.pay(invoice.number());

        verify(spiedRepository).save(invoice);
    }

    @Test
    void savesTheInvoiceEveryTimeItChanges() {
        InMemoryInvoiceRepository spiedRepository = spy(new InMemoryInvoiceRepository());
        InvoiceService spiedService = InvoiceServiceFixture.service(spiedRepository, gateway, notifier, MARCH_15);

        Invoice draft = createMugsAndPens(spiedService);
        spiedService.issue(draft.number());

        verify(spiedRepository, times(2)).save(draft);
        assertThat(spiedRepository.findByNumber(draft.number())).hasValue(draft);
    }

    private String issuedInvoice() {
        Invoice draft = createMugsAndPens(service);
        return service.issue(draft.number()).number();
    }
}
