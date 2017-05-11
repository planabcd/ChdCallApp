package com.xiaoniu.wifihotspotdemo.common;

public class Constant {
	public static String URL_SERVER = "http://119.29.184.87:8091/chd-attence-web/";
//	public static String URL_SERVER = "http://192.168.56.1";

	public static String URL_LOGIN_STUDENT = URL_SERVER+"/rest/student/login";
	public static String URL_LOGIN_TEACHER = URL_SERVER+"/rest/teacher/login";



	public static String URL_CLASS_QUERY_BY_COURSE_ID = URL_SERVER+"/rest/classInfo/queryByCourseId";


	public static String URL_LOGIN_STUDENT_COURSE = URL_SERVER+"/rest/course/queryByClassId";
	public static String URL_LOGIN_TEACHER_COURSE = URL_SERVER+"/rest/course/queryByTeacherId";


	public static String URL_LOGIN_TEACHER_STUDENT_ATTENCE = URL_SERVER+"/rest/tattence/studentAttenceList";
	public static String URL_LOGIN_TEACHER_CALL_ATTENCE = URL_SERVER+"/rest/tattence/call";



	public static String URL_STUDENT_QUERY_CHECK_ATTENCE = URL_SERVER+"/rest/sattence/checkAttence";

	public static String URL_LOGIN_STUDENT_CALL_ATTENCE = URL_SERVER+"/rest/sattence/call";

	public static String URL_LOGIN_TEACHER_CANCEL_CALL_ATTENCE = URL_SERVER+"/rest/tattence/cancelCall";
	public static String URL_LOGIN_STUDENT_DOCALL_ATTENCE = URL_SERVER+"/rest/sattence/doCall";


	public static String URL_LOGIN_STUDENT_HISTORY = URL_SERVER+"/rest/sattence/history";
	public static String URL_STUDENT_EXCEPTION_CALL = URL_SERVER+"/rest/sattence/exceptionCall";

	public static String URL_TEACHER_SPECIAL_CALL = URL_SERVER+"/rest/tattence/specialCall";



	public static String URL_LOGIN_TEACHER_HISTORY = URL_SERVER+"/rest/tattence/history";

	public static String URL_STUDNET_BIND_MAC_ADDRESS = URL_SERVER+"/rest/student/bind";




	public static int ATTENCE_LENGTH = 1; //教师端考勤时间单位分钟


}
