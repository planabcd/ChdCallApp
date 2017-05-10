package com.xiaoniu.wifihotspotdemo.domain;

/**
 * Created by think on 2017/5/6 22:01.
 */

public class TeacherAttenceBackUp {
    private String classIdAndClassName;
    private String courseIdAndCourseName;
    private String startTime;
    private String teacherIdAndName;
    private String wifiName;
    private String wifiPwd;
    private Long teacherAttenceId;

    public Long getTeacherAttenceId() {
        return teacherAttenceId;
    }

    public void setTeacherAttenceId(Long teacherAttenceId) {
        this.teacherAttenceId = teacherAttenceId;
    }

    public String getClassIdAndClassName() {
        return classIdAndClassName;
    }

    public void setClassIdAndClassName(String classIdAndClassName) {
        this.classIdAndClassName = classIdAndClassName;
    }

    public String getCourseIdAndCourseName() {
        return courseIdAndCourseName;
    }

    public void setCourseIdAndCourseName(String courseIdAndCourseName) {
        this.courseIdAndCourseName = courseIdAndCourseName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getTeacherIdAndName() {
        return teacherIdAndName;
    }

    public void setTeacherIdAndName(String teacherIdAndName) {
        this.teacherIdAndName = teacherIdAndName;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public String getWifiPwd() {
        return wifiPwd;
    }

    public void setWifiPwd(String wifiPwd) {
        this.wifiPwd = wifiPwd;
    }
}
