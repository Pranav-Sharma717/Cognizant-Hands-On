package com.designpatterns.adapter;

public class PayPalAdapter implements PaymentProcessor {
    private LegacyPayPalGateway gateway;
    private String username;

    public PayPalAdapter(LegacyPayPalGateway gateway, String username) {
        this.gateway = gateway;
        this.username = username;
    }

    @Override
    public void processPayment(double amount) {
        gateway.makePayment(username, amount);
    }
}
