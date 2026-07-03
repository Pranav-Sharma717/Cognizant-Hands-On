package com.designpatterns.strategy;

public class Main {
    public static void main(String[] args) {
        PaymentContext context = new PaymentContext();

        System.out.println("Selecting Credit Card Strategy:");
        context.setPaymentStrategy(new CreditCardPayment("Pranav Sharma", "1234567890123456"));
        context.executePayment(89.99);

        System.out.println("\nSelecting PayPal Strategy:");
        context.setPaymentStrategy(new PayPalPayment("pranav@sharma.com"));
        context.executePayment(45.50);
    }
}
