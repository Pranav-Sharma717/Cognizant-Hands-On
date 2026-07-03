package com.designpatterns.adapter;

public class PayPalAdapter implements PaymentProcessor {
    // TODO: Implement Adapter pattern (compose with LegacyPayPalGateway)
    @Override
    public void processPayment(double amount) {
        // TODO: Map to legacy gateway makePayment
    }
}
