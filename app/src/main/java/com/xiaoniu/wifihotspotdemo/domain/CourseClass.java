package com.xiaoniu.wifihotspotdemo.domain;

/**
 * Created by liaoxin on 2017/4/10.
 */
public class CourseClass extends BaseModel {
    private Long classId;
    private Long courseId;

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
}
