package com.designpatterns.singleton;

public class Main {
    public static void main(String[] args) {
        // TODO: Test your Singleton implementation
        // System.out.println("Testing Singleton Pattern...");
        DatabaseConnection d1 = DatabaseConnection.getInstance();
        DatabaseConnection d2 = DatabaseConnection.getInstance();

        d1.log("First Log");
        d2.log("Second Log");

        System.out.println(d1 == d2);
    }
}
