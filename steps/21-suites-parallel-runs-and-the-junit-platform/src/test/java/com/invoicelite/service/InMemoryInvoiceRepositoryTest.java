package com.invoicelite.service;

class InMemoryInvoiceRepositoryTest extends InvoiceRepositoryContract {

    @Override
    protected InvoiceRepository repository() {
        return new InMemoryInvoiceRepository();
    }
}
