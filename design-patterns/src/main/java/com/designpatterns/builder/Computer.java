package com.designpatterns.builder;

public class Computer {
    private final String HDD;
    private final String RAM;
    private final boolean isGraphicsCardEnabled;
    private final boolean isBluetoothEnabled;

    public String getHDD() { return HDD; }
    public String getRAM() { return RAM; }
    public boolean isGraphicsCardEnabled() { return isGraphicsCardEnabled; }
    public boolean isBluetoothEnabled() { return isBluetoothEnabled; }

    private Computer(Builder builder) {
        this.HDD = builder.HDD;
        this.RAM = builder.RAM;
        this.isGraphicsCardEnabled = builder.isGraphicsCardEnabled;
        this.isBluetoothEnabled = builder.isBluetoothEnabled;
    }

    @Override
    public String toString() {
        return "Computer [HDD=" + HDD + ", RAM=" + RAM + ", GraphicsCard=" + isGraphicsCardEnabled + ", Bluetooth=" + isBluetoothEnabled + "]";
    }

    public static class Builder {
        private final String HDD;
        private final String RAM;
        private boolean isGraphicsCardEnabled;
        private boolean isBluetoothEnabled;

        public Builder(String HDD, String RAM) {
            this.HDD = HDD;
            this.RAM = RAM;
        }

        public Builder setGraphicsCardEnabled(boolean isGraphicsCardEnabled) {
            this.isGraphicsCardEnabled = isGraphicsCardEnabled;
            return this;
        }

        public Builder setBluetoothEnabled(boolean isBluetoothEnabled) {
            this.isBluetoothEnabled = isBluetoothEnabled;
            return this;
        }

        public Computer build() {
            return new Computer(this);
        }
    }
}
