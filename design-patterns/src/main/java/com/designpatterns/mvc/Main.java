package com.designpatterns.mvc;

public class Main {
    public static void main(String[] args) {
        Student student = new Student();
        student.setId("S202601");
        student.setName("G V Pranav Sharma");
        student.setGrade("A+");

        StudentView view = new StudentView();
        StudentController controller = new StudentController(student, view);

        System.out.println("Initial Record:");
        controller.updateView();

        System.out.println("\nUpdating Student Name and Grade via Controller:");
        controller.setStudentName("Pranav Sharma");
        controller.setStudentGrade("O (Outstanding)");
        controller.updateView();
    }
}
