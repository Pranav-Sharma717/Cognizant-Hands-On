package com.designpatterns.dependencyinjection;

public class V8Engine implements Engine {
    @Override
    public void start() {
        System.out.println("V8 Engine starting: VROOOOM!");
    }
}
