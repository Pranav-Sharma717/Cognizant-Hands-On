package com.designpatterns.factorymethod;

public class PdfDocument implements Document {
    @Override
    public void open() {
        System.out.println("Opening PDF document: layout and pages rendered.");
    }
}
