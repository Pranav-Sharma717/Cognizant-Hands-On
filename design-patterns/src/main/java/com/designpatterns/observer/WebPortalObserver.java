package com.designpatterns.observer;

public class WebPortalObserver implements Observer {
    @Override
    public void update(double price) {
        System.out.println("Web Portal: Stock price updated to $" + price);
    }
}
