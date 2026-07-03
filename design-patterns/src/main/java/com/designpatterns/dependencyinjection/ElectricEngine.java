package com.designpatterns.dependencyinjection;

public class ElectricEngine implements Engine {
    @Override
    public void start() {
        System.out.println("Electric Engine starting: ... hummm ... silent power.");
    }
}
