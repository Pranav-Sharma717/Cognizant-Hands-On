import java.util.*;

abstract class Shape {
    abstract double area();

    void display() {
        System.out.println("Area: " + area());
    }
}

class Circle extends Shape {
    double radius;

    Circle(double r) {
        radius = r;
    }

    @Override
    double area() {
        return Math.PI * radius * radius;
    }
}

public class Shape {
    public static void main(String[] args) {
        Circle circle = new Circle(5);
        circle.display();
    }
}
