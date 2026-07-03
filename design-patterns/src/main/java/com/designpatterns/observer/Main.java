package com.designpatterns.observer;

public class Main {
    public static void main(String[] args) {
        StockMarket appleStock = new StockMarket("AAPL", 150.00);

        Observer mobileApp = new MobileAppObserver();
        Observer webPortal = new WebPortalObserver();

        appleStock.registerObserver(mobileApp);
        appleStock.registerObserver(webPortal);

        System.out.println("Updating Stock Price to $155.50:");
        appleStock.setStockPrice(155.50);

        System.out.println("\nRemoving Web Portal Observer...");
        appleStock.removeObserver(webPortal);

        System.out.println("\nUpdating Stock Price to $158.20:");
        appleStock.setStockPrice(158.20);
    }
}
