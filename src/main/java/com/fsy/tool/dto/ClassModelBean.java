package com.fsy.tool.dto;


public class ClassModelBean {
    private static String courseName;
    private static String courseId;

    private static int index = -1;

    public static int getIndex() {
        return index;
    }

    public static void setIndex(int index) {
        ClassModelBean.index = index;
    }

    public static String getCourseName() {
        return courseName;
    }

    public static void setCourseName(String courseName) {
        ClassModelBean.courseName = courseName;
    }

    public static String getCourseId() {
        return courseId;
    }

    public static void setCourseId(String courseId) {
        ClassModelBean.courseId = courseId;
    }
}
