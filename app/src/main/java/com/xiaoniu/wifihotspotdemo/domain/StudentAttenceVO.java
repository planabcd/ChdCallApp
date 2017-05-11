package com.xiaoniu.wifihotspotdemo.domain;

/**
 * Created by liaoxin on 2017/4/10.
 */
public class StudentAttenceVO extends BaseModel{
    private String name;
    private Long state;
    private Long studentId;
    private String gps;
    private Long courseId;
    private String wifiTop5;
    private Long teacherAttenceId;
    private String teacherName;
    private String courseName;
    private String remark;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Long getTeacherAttenceId() {
        return teacherAttenceId;
    }

    public void setTeacherAttenceId(Long teacherAttenceId) {
        this.teacherAttenceId = teacherAttenceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getState() {
        return state;
    }

    public void setState(Long state) {
        this.state = state;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getGps() {
        return gps;
    }

    public void setGps(String gps) {
        this.gps = gps;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getWifiTop5() {
        return wifiTop5;
    }

    public void setWifiTop5(String wifiTop5) {
        this.wifiTop5 = wifiTop5;
    }
}
