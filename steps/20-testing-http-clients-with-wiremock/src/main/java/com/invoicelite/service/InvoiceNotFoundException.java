package com.invoicelite.service;

public class InvoiceNotFoundException extends RuntimeException {

    public InvoiceNotFoundException(String number) {
        super("Invoice " + number + " was not found");
    }
}
