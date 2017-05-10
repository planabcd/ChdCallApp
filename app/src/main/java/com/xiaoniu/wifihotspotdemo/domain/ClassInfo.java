package com.xiaoniu.wifihotspotdemo.domain;

/**
 * Created by liaoxin on 2017/4/10.
 */
public class ClassInfo extends BaseModel {
      private String academy;
      private String marjor;
      private Long grade;
      private Long classId;

    public String getAcademy() {
        return academy;
    }

    public void setAcademy(String academy) {
        this.academy = academy;
    }

    public String getMarjor() {
        return marjor;
    }

    public void setMarjor(String marjor) {
        this.marjor = marjor;
    }

    public Long getGrade() {
        return grade;
    }

    public void setGrade(Long grade) {
        this.grade = grade;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }
}
