package com.designpatterns.builder;

public class Main {
    public static void main(String[] args) {
        Computer gamingPC = new Computer.Builder("1TB SSD", "32GB")
                                .setGraphicsCardEnabled(true)
                                .setBluetoothEnabled(true)
                                .build();

        Computer officePC = new Computer.Builder("500GB HDD", "8GB")
                                .setGraphicsCardEnabled(false)
                                .setBluetoothEnabled(true)
                                .build();

        System.out.println("Gaming PC: " + gamingPC);
        System.out.println("Office PC: " + officePC);
    }
}
