package com.designpatterns.adapter;

public class Main {
    public static void main(String[] args) {
        PaymentProcessor stripe = new StripePaymentProcessor();
        stripe.processPayment(150.00);

        LegacyPayPalGateway legacyGateway = new LegacyPayPalGateway();
        PaymentProcessor paypalAdapter = new PayPalAdapter(legacyGateway, "Pranav Sharma");
        paypalAdapter.processPayment(250.50);
    }
}
