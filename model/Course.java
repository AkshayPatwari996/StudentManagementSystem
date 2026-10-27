
package com.student.registration.model;

public class Course {

    private int courseId;
    private String courseName;
    private String description;
    private int credits;

    // No-argument constructor
    public Course() {
    }

    // Constructor for adding a course
    public Course(String courseName, String description, int credits) {
        this.courseName = courseName;
        this.description = description;
        this.credits = credits;
    }

    // Full constructor
    public Course(int courseId, String courseName,
                  String description, int credits) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.description = description;
        this.credits = credits;
    }

    // Getters and Setters
    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }
}
