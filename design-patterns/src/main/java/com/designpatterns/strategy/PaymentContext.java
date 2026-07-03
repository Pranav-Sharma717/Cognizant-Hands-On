package com.designpatterns.strategy;

public class PaymentContext {
    private PaymentStrategy strategy;

    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public void executePayment(double amount) {
        if (strategy == null) {
            System.out.println("Please select a payment strategy.");
        } else {
            strategy.pay(amount);
        }
    }
}
