package com.designpatterns.observer;

import java.util.ArrayList;
import java.util.List;

public class StockMarket implements Subject {
    private List<Observer> observers = new ArrayList<>();
    private double price;
    private String stockSymbol;

    public StockMarket(String stockSymbol, double initialPrice) {
        this.stockSymbol = stockSymbol;
        this.price = initialPrice;
    }

    public void setStockPrice(double price) {
        this.price = price;
        notifyObservers();
    }

    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update(price);
        }
    }
}
