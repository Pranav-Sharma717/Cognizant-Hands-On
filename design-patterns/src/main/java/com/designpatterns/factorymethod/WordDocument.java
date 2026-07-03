package com.designpatterns.factorymethod;

public class WordDocument implements Document {
    @Override
    public void open() {
        System.out.println("Opening Word document: text formatting and styles loaded.");
    }
}
