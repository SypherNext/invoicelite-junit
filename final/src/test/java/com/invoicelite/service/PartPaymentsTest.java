package com.invoicelite.service;

import com.invoicelite.invoice.Invoice;
import com.invoicelite.invoice.InvoiceStatus;
import com.invoicelite.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.invoicelite.service.InvoiceServiceFixture.MARCH_15;
import static com.invoicelite.service.InvoiceServiceFixture.createMugsAndPens;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartPaymentsTest {

    @Mock
    private PaymentGateway gateway;

    @Mock
    private Notifier notifier;

    @Captor
    private ArgumentCaptor<PaymentRequest> requests;

    private final InMemoryInvoiceRepository repository = new InMemoryInvoiceRepository();
    private InvoiceService service;
    private String number;

    @BeforeEach
    void issueAnInvoiceFor7473() {
        service = InvoiceServiceFixture.service(repository, gateway, notifier, MARCH_15);
        Invoice draft = createMugsAndPens(service);
        number = service.issue(draft.number()).number();
    }

    @Test
    void twoPartsPayTheInvoice() {
        when(gateway.charge(any())).thenReturn(PaymentResult.approved("ch_1"), PaymentResult.approved("ch_2"));

        PaymentResult first = service.pay(number, Money.of("30.00", "EUR"));
        assertThat(first.reference()).isEqualTo("ch_1");
        assertThat(stored().status()).isEqualTo(InvoiceStatus.ISSUED);
        service.pay(number, Money.of("44.73", "EUR"));

        assertThat(stored().status()).isEqualTo(InvoiceStatus.PAID);
        assertThat(stored().payments()).extracting(payment -> payment.reference()).containsExactly("ch_1", "ch_2");
    }

    @Test
    void eachPartIsChargedWithItsOwnIdempotencyKey() {
        when(gateway.charge(any())).thenReturn(PaymentResult.approved("ch_1"), PaymentResult.approved("ch_2"));

        service.pay(number, Money.of("30.00", "EUR"));
        service.pay(number, Money.of("44.73", "EUR"));

        verify(gateway, times(2)).charge(requests.capture());
        assertThat(requests.getAllValues()).extracting(PaymentRequest::amount)
                .containsExactly(Money.of("30.00", "EUR"), Money.of("44.73", "EUR"));
        assertThat(requests.getAllValues()).extracting(PaymentRequest::idempotencyKey).doesNotHaveDuplicates();
    }

    @Test
    void payingWithoutAnAmountPaysWhatIsLeft() {
        when(gateway.charge(any())).thenReturn(PaymentResult.approved("ch_1"), PaymentResult.approved("ch_2"));
        service.pay(number, Money.of("30.00", "EUR"));

        service.pay(number);

        verify(gateway, times(2)).charge(requests.capture());
        assertThat(requests.getAllValues().getLast().amount()).isEqualTo(Money.of("44.73", "EUR"));
        assertThat(stored().status()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void anOverpaymentIsRefusedBeforeAnythingIsCharged() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.pay(number, Money.of("80.00", "EUR")))
                .withMessage("Payment of 80.00 is more than the 74.73 due on INV-2026-0001");
        verifyNoInteractions(gateway);
    }

    private Invoice stored() {
        return repository.findByNumber(number).orElseThrow();
    }
}
