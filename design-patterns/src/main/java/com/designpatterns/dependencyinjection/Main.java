package com.designpatterns.dependencyinjection;

public class Main {
    public static void main(String[] args) {
        System.out.println("Creating Sports Car (with V8 Engine):");
        Engine v8 = new V8Engine();
        Car sportsCar = new Car(v8);
        sportsCar.drive();

        System.out.println("\nCreating Electric Car (with Electric Engine):");
        Engine electric = new ElectricEngine();
        Car ecoCar = new Car(electric);
        ecoCar.drive();
    }
}
