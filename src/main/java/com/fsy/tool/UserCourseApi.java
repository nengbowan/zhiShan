package com.fsy.tool;

import com.fsy.tool.dto.CourseModelBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 单用户的单个课程
 */
public class UserCourseApi extends AllCourseApi{

    private String courseId;
    public UserCourseApi(String username, String password) {
        super(username, password);
    }

    public UserCourseApi(String username, String password,String courseId) {
        super(username, password);
        this.courseId = courseId;
    }
    @Override
    public void getCourseList(){
        List<CourseModelBean> courseList = new ArrayList<>();
        courseList.add(CourseModelBean.builder().code(courseId).build());
        super.setCourseList(courseList);
    }
}
