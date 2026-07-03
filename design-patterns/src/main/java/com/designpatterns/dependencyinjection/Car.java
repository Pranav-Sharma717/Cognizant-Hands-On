package com.designpatterns.dependencyinjection;

public class Car {
    private Engine engine;

    // Constructor injection
    public Car(Engine engine) {
        this.engine = engine;
    }

    public void drive() {
        engine.start();
        System.out.println("Car is moving down the road.");
    }
}
