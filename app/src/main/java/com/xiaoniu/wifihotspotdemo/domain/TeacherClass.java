package com.xiaoniu.wifihotspotdemo.domain;

/**
 * Created by liaoxin on 2017/4/10.
 */
public class TeacherClass extends BaseModel {
    private Long classId;
    private Long teacherId;

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
}
