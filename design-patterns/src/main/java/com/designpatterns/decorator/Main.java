package com.designpatterns.decorator;

public class Main {
    public static void main(String[] args) {
        Notifier baseEmail = new EmailNotifier();
        System.out.println("--- Single Notifier ---");
        baseEmail.send("Hello World!");

        System.out.println("\n--- Email + SMS Notifier ---");
        Notifier emailAndSms = new SMSNotifierDecorator(baseEmail);
        emailAndSms.send("Alert: Server High Usage!");

        System.out.println("\n--- Fully Decorated (Email + SMS + Slack) Notifier ---");
        Notifier fullNotifier = new SlackNotifierDecorator(emailAndSms);
        fullNotifier.send("Emergency: System Outage!");
    }
}
