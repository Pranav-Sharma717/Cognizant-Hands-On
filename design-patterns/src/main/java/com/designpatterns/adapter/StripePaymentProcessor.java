package com.designpatterns.adapter;

public class StripePaymentProcessor implements PaymentProcessor {
    @Override
    public void processPayment(double amount) {
        System.out.println("Stripe processing payment of $" + amount);
    }
}
