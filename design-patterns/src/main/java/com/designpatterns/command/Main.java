package com.designpatterns.command;

public class Main {
    public static void main(String[] args) {
        Light livingRoomLight = new Light();
        Command lightOn = new LightOnCommand(livingRoomLight);
        Command lightOff = new LightOffCommand(livingRoomLight);

        RemoteControl remote = new RemoteControl();

        System.out.println("Pressing button for Light ON:");
        remote.setCommand(lightOn);
        remote.pressButton();

        System.out.println("\nPressing button for Light OFF:");
        remote.setCommand(lightOff);
        remote.pressButton();
    }
}
