package com.designpatterns.observer;

public class MobileAppObserver implements Observer {
    @Override
    public void update(double price) {
        System.out.println("Mobile App: Stock price updated to $" + price);
    }
}
