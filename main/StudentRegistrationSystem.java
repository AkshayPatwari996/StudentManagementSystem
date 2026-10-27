
package com.student.registration.main;

import com.student.registration.service.StudentService;
import com.student.registration.util.DBConnection;

import java.sql.SQLException;
import java.util.Scanner;

public class StudentRegistrationSystem {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        StudentService service = new StudentService();

        try {
            service.initialize();

            while (true) {
                System.out.println("\n===== STUDENT COURSE REGISTRATION SYSTEM =====");
                System.out.println("1. Register Student");
                System.out.println("2. Add Course");
                System.out.println("3. Enroll Student in Course");
                System.out.println("4. View Student Details");
                System.out.println("5. View Students in a Particular Course");
                System.out.println("6. View Courses Taken by a Student");
                System.out.println("7. Generate Student-Course Report");
                System.out.println("8. View All Students");
                System.out.println("9. View All Courses");
                System.out.println("10. Exit");

                System.out.print("Enter your choice: ");

                String input = sc.nextLine().trim();
                int choice;

                try {
                    choice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println(
                            "Invalid input. Please enter a number.");
                    continue;
                }

                try {
                    switch (choice) {

                        case 1:
                            service.registerStudent(sc);
                            break;

                        case 2:
                            service.addCourse(sc);
                            break;

                        case 3:
                            service.enrollStudent(sc);
                            break;

                        case 4:
                            service.viewStudentDetails(sc);
                            break;

                        case 5:
                            service.viewStudentsInCourse(sc);
                            break;

                        case 6:
                            service.viewCoursesByStudent(sc);
                            break;

                        case 7:
                            service.generateStudentCourseReport();
                            break;

                        case 8:
                            service.viewAllStudents();
                            break;

                        case 9:
                            service.viewAllCourses();
                            break;

                        case 10:
                            System.out.println(
                                    "Thank you for using Student Course Registration System.");
                            return;

                        default:
                            System.out.println(
                                    "Invalid choice. Please enter a number between 1 and 10.");
                    }

                } catch (SQLException e) {
                    System.out.println(
                            "Database operation failed: " + e.getMessage());

                } catch (IllegalArgumentException e) {
                    System.out.println(
                            "Invalid input: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not initialize the database: " + e.getMessage());

        } finally {
            sc.close();
            DBConnection.shutdown();
        }
    }
}
