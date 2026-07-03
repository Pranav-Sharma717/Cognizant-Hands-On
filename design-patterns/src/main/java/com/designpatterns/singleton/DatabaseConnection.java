package com.designpatterns.singleton;

public class DatabaseConnection {
    // Singleton instance
    private static DatabaseConnection instance;

    // Private constructor prevents external instantiation
    private DatabaseConnection() {
        System.out.println("Logger Created");
    }

    // Thread-safe lazy initialization
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public void log(String msg) {
        System.out.println(msg);
    }
}
