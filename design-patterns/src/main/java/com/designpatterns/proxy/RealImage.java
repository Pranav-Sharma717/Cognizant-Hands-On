package com.designpatterns.proxy;

public class RealImage implements Image {
    private String filename;

    public RealImage(String filename) {
        this.filename = filename;
        loadFromDisk();
    }

    private void loadFromDisk() {
        // TODO: Simulate heavy loading operation
    }

    @Override
    public void display() {
        // TODO: Display image logic
    }
}
