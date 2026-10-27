
package com.student.registration.dao;

import com.student.registration.util.DBConnection;
import com.student.registration.model.Course;
import com.student.registration.model.Enrollment;
import com.student.registration.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    // Create database tables and stored procedure
    public void initializeTables() throws SQLException {

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement()) {

            String studentsTable =
                    "CREATE TABLE IF NOT EXISTS students (" +
                            "student_id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "name VARCHAR(100) NOT NULL, " +
                            "email VARCHAR(100) NOT NULL UNIQUE, " +
                            "phone VARCHAR(15) NOT NULL)";

            String coursesTable =
                    "CREATE TABLE IF NOT EXISTS courses (" +
                            "course_id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "course_name VARCHAR(100) NOT NULL UNIQUE, " +
                            "description VARCHAR(255), " +
                            "credits INT NOT NULL CHECK (credits > 0))";

            String enrollmentsTable =
                    "CREATE TABLE IF NOT EXISTS enrollments (" +
                            "enrollment_id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "student_id INT NOT NULL, " +
                            "course_id INT NOT NULL, " +
                            "enrollment_date DATE NOT NULL DEFAULT (CURRENT_DATE), " +
                            "UNIQUE (student_id, course_id), " +
                            "FOREIGN KEY (student_id) REFERENCES students(student_id), " +
                            "FOREIGN KEY (course_id) REFERENCES courses(course_id))";

            stmt.executeUpdate(studentsTable);
            stmt.executeUpdate(coursesTable);
            stmt.executeUpdate(enrollmentsTable);

            stmt.executeUpdate("DROP PROCEDURE IF EXISTS get_student_courses");

            String procedure =
                    "CREATE PROCEDURE get_student_courses(IN p_student_id INT) " +
                            "BEGIN " +
                            "SELECT c.course_id, c.course_name, c.description, c.credits " +
                            "FROM courses c " +
                            "JOIN enrollments e ON c.course_id = e.course_id " +
                            "WHERE e.student_id = p_student_id " +
                            "ORDER BY c.course_name; " +
                            "END";

            stmt.executeUpdate(procedure);
        }
    }

    // Register a student
    public int registerStudent(Student student) throws SQLException {

        String sql =
                "INSERT INTO students (name, email, phone) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, student.getName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getPhone());

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Student registration failed.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Student ID could not be generated.");
    }

    // Add a course
    public int addCourse(Course course) throws SQLException {

        String sql =
                "INSERT INTO courses (course_name, description, credits) " +
                        "VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, course.getCourseName());
            ps.setString(2, course.getDescription());
            ps.setInt(3, course.getCredits());

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Course insertion failed.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Course ID could not be generated.");
    }

    // Enroll a student in a course using a transaction
    public void enrollStudent(Enrollment enrollment) throws SQLException {

        String sql =
                "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";

        Connection con = null;

        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, enrollment.getStudentId());
                ps.setInt(2, enrollment.getCourseId());

                ps.executeUpdate();
            }

            con.commit();

        } catch (SQLException e) {

            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
            }

            throw e;

        } finally {

            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) {
                    // Closing the connection returns it to the pool.
                }

                con.close();
            }
        }
    }

    // View student details by ID
    public Student getStudentById(int studentId) throws SQLException {

        String sql =
                "SELECT student_id, name, email, phone " +
                        "FROM students WHERE student_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("student_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone")
                    );
                }
            }
        }

        return null;
    }

    // List students enrolled in a particular course
    public List<Student> getStudentsInCourse(int courseId)
            throws SQLException {

        List<Student> students = new ArrayList<>();

        String sql =
                "SELECT s.student_id, s.name, s.email, s.phone " +
                        "FROM students s " +
                        "JOIN enrollments e ON s.student_id = e.student_id " +
                        "WHERE e.course_id = ? " +
                        "ORDER BY s.name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(new Student(
                            rs.getInt("student_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone")
                    ));
                }
            }
        }

        return students;
    }

    // List courses taken by a student using CallableStatement
    public List<Course> getCoursesByStudent(int studentId)
            throws SQLException {

        List<Course> courses = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             CallableStatement cs =
                     con.prepareCall("{call get_student_courses(?)}")) {

            cs.setInt(1, studentId);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    courses.add(new Course(
                            rs.getInt("course_id"),
                            rs.getString("course_name"),
                            rs.getString("description"),
                            rs.getInt("credits")
                    ));
                }
            }
        }

        return courses;
    }

    // Generate a student-course report using JOIN
    public void generateStudentCourseReport() throws SQLException {

        String sql =
                "SELECT s.student_id, s.name, s.email, " +
                        "c.course_id, c.course_name, e.enrollment_date " +
                        "FROM students s " +
                        "JOIN enrollments e ON s.student_id = e.student_id " +
                        "JOIN courses c ON e.course_id = c.course_id " +
                        "ORDER BY s.name, c.course_name";

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n===== STUDENT-COURSE REPORT =====");

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.println(
                        "Student ID: " + rs.getInt("student_id") +
                                " | Name: " + rs.getString("name") +
                                " | Email: " + rs.getString("email") +
                                " | Course ID: " + rs.getInt("course_id") +
                                " | Course: " + rs.getString("course_name") +
                                " | Enrollment Date: " + rs.getDate("enrollment_date")
                );
            }

            if (!found) {
                System.out.println("No enrollments found.");
            }
        }
    }

    // View all students
    public List<Student> getAllStudents() throws SQLException {

        List<Student> students = new ArrayList<>();

        String sql =
                "SELECT student_id, name, email, phone " +
                        "FROM students ORDER BY student_id";

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(new Student(
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone")
                ));
            }
        }

        return students;
    }

    // View all courses
    public List<Course> getAllCourses() throws SQLException {

        List<Course> courses = new ArrayList<>();

        String sql =
                "SELECT course_id, course_name, description, credits " +
                        "FROM courses ORDER BY course_id";

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                courses.add(new Course(
                        rs.getInt("course_id"),
                        rs.getString("course_name"),
                        rs.getString("description"),
                        rs.getInt("credits")
                ));
            }
        }

        return courses;
    }
}
