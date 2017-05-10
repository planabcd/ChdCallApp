package com.xiaoniu.wifihotspotdemo;

import com.google.gson.Gson;
import com.xiaoniu.wifihotspotdemo.domain.Teacher;
import com.xiaoniu.wifihotspotdemo.util.GsonBuilderUtil;

/**
 * Created by think on 2017/5/6 18:00.
 */

public class Test {
    public static void main(String[] args) {
       /* test(new TestWorker<List<Course>>() {
            @Override
            public void exe(List<Course> courses) {
                if(courses!=null){
                    for (Course course : courses) {
                        String name = course.getName();
                    }
                }
            }
        });*/
        String re = "{}";
        Gson gson = GsonBuilderUtil.create();
        gson.fromJson(re, Teacher.class);
        
    }

    public static<T> void test(TestWorker<T> tw){
        Gson gson = GsonBuilderUtil.create();
        String result = "[{\"created\":1492321787000,\"updated\":1492322122000,\"id\":5,\"name\":\"自动化控制\",\"teacher\":\"杨阳\",\"teacherId\":10}]";
        /*Type type = new TypeToken<List<Course>>() {
        }.getType();*/
    }

    interface TestWorker<T>{
        void exe(T t);
    }
}
