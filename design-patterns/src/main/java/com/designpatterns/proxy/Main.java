package com.designpatterns.proxy;

public class Main {
    public static void main(String[] args) {
        Image image = new ProxyImage("high_res_photo.png");

        System.out.println("First call to display():");
        image.display();

        System.out.println("\nSecond call to display():");
        image.display();
    }
}
