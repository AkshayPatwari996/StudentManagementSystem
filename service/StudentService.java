
package com.student.registration.service;

import com.student.registration.dao.StudentDAO;
import com.student.registration.model.Course;
import com.student.registration.model.Enrollment;
import com.student.registration.model.Student;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class StudentService {

    private final StudentDAO dao = new StudentDAO();

    // Initialize tables and stored procedure
    public void initialize() throws SQLException {
        dao.initializeTables();
        System.out.println("Database tables initialized successfully.");
    }

    // Read a non-empty string
    private String readRequiredText(Scanner sc, String message) {
        while (true) {
            System.out.print(message);
            String value = sc.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("Input cannot be empty.");
        }
    }

    // Read a positive integer
    private int readPositiveInt(Scanner sc, String message) {
        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();

            try {
                int value = Integer.parseInt(input);

                if (value > 0) {
                    return value;
                }

                System.out.println("Enter a number greater than zero.");

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    // Register a student
    public void registerStudent(Scanner sc) throws SQLException {

        String name = readRequiredText(sc, "Enter student name: ");
        String email = readRequiredText(sc, "Enter email: ");
        String phone = readRequiredText(sc, "Enter phone number: ");

        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }

        if (!phone.matches("[0-9]{10,15}")) {
            throw new IllegalArgumentException(
                    "Phone number must contain 10 to 15 digits.");
        }

        Student student = new Student(name, email, phone);

        int studentId = dao.registerStudent(student);

        System.out.println("Student registered successfully.");
        System.out.println("Generated Student ID: " + studentId);
    }

    // Add a course
    public void addCourse(Scanner sc) throws SQLException {

        String courseName = readRequiredText(sc, "Enter course name: ");

        System.out.print("Enter course description (optional): ");
        String description = sc.nextLine().trim();

        int credits = readPositiveInt(sc, "Enter course credits: ");

        Course course = new Course(courseName, description, credits);

        int courseId = dao.addCourse(course);

        System.out.println("Course added successfully.");
        System.out.println("Generated Course ID: " + courseId);
    }

    // Enroll a student in a course
    public void enrollStudent(Scanner sc) throws SQLException {

        int studentId = readPositiveInt(sc, "Enter student ID: ");
        int courseId = readPositiveInt(sc, "Enter course ID: ");

        Enrollment enrollment = new Enrollment(studentId, courseId);

        dao.enrollStudent(enrollment);

        System.out.println("Student enrolled successfully.");
    }

    // View student details
    public void viewStudentDetails(Scanner sc) throws SQLException {

        int studentId = readPositiveInt(sc, "Enter student ID: ");

        Student student = dao.getStudentById(studentId);

        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        System.out.println("\n===== STUDENT DETAILS =====");
        System.out.println("Student ID: " + student.getStudentId());
        System.out.println("Name: " + student.getName());
        System.out.println("Email: " + student.getEmail());
        System.out.println("Phone: " + student.getPhone());
    }

    // View students in a particular course
    public void viewStudentsInCourse(Scanner sc) throws SQLException {

        int courseId = readPositiveInt(sc, "Enter course ID: ");

        List<Student> students = dao.getStudentsInCourse(courseId);

        if (students.isEmpty()) {
            System.out.println("No students found for this course.");
            return;
        }

        System.out.println("\n===== STUDENTS IN COURSE =====");

        for (Student student : students) {
            System.out.println(
                    "ID: " + student.getStudentId()
                            + " | Name: " + student.getName()
                            + " | Email: " + student.getEmail()
                            + " | Phone: " + student.getPhone()
            );
        }
    }

    // View courses taken by a student
    public void viewCoursesByStudent(Scanner sc) throws SQLException {

        int studentId = readPositiveInt(sc, "Enter student ID: ");

        List<Course> courses = dao.getCoursesByStudent(studentId);

        if (courses.isEmpty()) {
            System.out.println("No courses found for this student.");
            return;
        }

        System.out.println("\n===== COURSES TAKEN BY STUDENT =====");

        for (Course course : courses) {
            System.out.println(
                    "Course ID: " + course.getCourseId()
                            + " | Course: " + course.getCourseName()
                            + " | Description: " + course.getDescription()
                            + " | Credits: " + course.getCredits()
            );
        }
    }

    // Generate student-course report
    public void generateStudentCourseReport() throws SQLException {
        dao.generateStudentCourseReport();
    }

    // View all students
    public void viewAllStudents() throws SQLException {

        List<Student> students = dao.getAllStudents();

        if (students.isEmpty()) {
            System.out.println("No students registered.");
            return;
        }

        System.out.println("\n===== ALL STUDENTS =====");

        for (Student student : students) {
            System.out.println(
                    "ID: " + student.getStudentId()
                            + " | Name: " + student.getName()
                            + " | Email: " + student.getEmail()
                            + " | Phone: " + student.getPhone()
            );
        }
    }

    // View all courses
    public void viewAllCourses() throws SQLException {

        List<Course> courses = dao.getAllCourses();

        if (courses.isEmpty()) {
            System.out.println("No courses available.");
            return;
        }

        System.out.println("\n===== ALL COURSES =====");

        for (Course course : courses) {
            System.out.println(
                    "ID: " + course.getCourseId()
                            + " | Name: " + course.getCourseName()
                            + " | Description: " + course.getDescription()
                            + " | Credits: " + course.getCredits()
            );
        }
    }
}
