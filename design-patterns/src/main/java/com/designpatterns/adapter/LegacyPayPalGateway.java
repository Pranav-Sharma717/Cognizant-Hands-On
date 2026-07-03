package com.designpatterns.adapter;

public class LegacyPayPalGateway {
    public void makePayment(String user, double amount) {
        System.out.println("Legacy PayPal processing payment for user '" + user + "' of $" + amount);
    }
}
