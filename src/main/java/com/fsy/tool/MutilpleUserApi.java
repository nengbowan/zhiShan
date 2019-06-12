package com.fsy.tool;

import com.fsy.tool.dto.UserModelBean;

import java.util.List;

/**
 * 多用户　单课程API
 */
public class MutilpleUserApi {

    private List<UserModelBean> users;

    private String courseId;

    public MutilpleUserApi(List<UserModelBean> users  , String courseId){
        this.users = users;
        this.courseId = courseId;
    }

    public void start(){
        for(UserModelBean user :users ){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new UserCourseApi(user.getUsername() , user.getPassword() , courseId).start();
                }
            }).start();
        }
    }
}
