package com.xiaoniu.wifihotspotdemo.util;

import android.content.Context;
import android.widget.Toast;

import com.mingle.widget.ShapeLoadingDialog;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by think on 2017/4/16 16:27
 */

public class UIUtil {
    public static void showToast(Context c,String text){
        Toast.makeText(c,text,Toast.LENGTH_LONG).show();
    }

    public static void showToastS(Context c,String text){
        Toast.makeText(c,text,Toast.LENGTH_SHORT).show();
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 弹出警告框框
     * @param ctx
     * @param title
     * @param alterCallBack
     */
    public static void alert(Context ctx, String title,String contentText ,final AlterCallBack alterCallBack){
        new SweetAlertDialog(ctx, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(contentText)
                .setCancelText("取消")
                .setConfirmText("确认")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(final SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
                alterCallBack.confirm();
            }
        }).show();
    }

    /**
     * 弹出确认框
     * @param ctx
     * @param title
     * @param alterCallBack
     */
    public static void ok(Context ctx, String title,String contentText ,final AlterCallBack alterCallBack){
        new SweetAlertDialog(ctx, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .setContentText(contentText)
                .setCancelText("取消")
                .setConfirmText("确认")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(final SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
                alterCallBack.confirm();
            }
        }).show();
    }

    /**
     * 弹出确认框,没有取消按钮
     * @param ctx
     * @param title
     * @param alterCallBack
     */
    public static void okNoCancel(Context ctx, String title,String contentText ,final AlterCallBack alterCallBack){
        new SweetAlertDialog(ctx, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .setContentText(contentText)
                .setConfirmText("确认")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(final SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
                alterCallBack.confirm();
            }
        }).show();
    }

    /**
     * 弹出消息框,没有取消按钮,没有按钮点击事件
     * @param ctx
     * @param title
     */
    public static void okTips(Context ctx, String title,String contentText ){
        new SweetAlertDialog(ctx, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .setContentText(contentText)
                .setConfirmText("确认")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(final SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }
        }).show();
    }



    /**
     * 确认回调函数
     */
    public interface AlterCallBack{
        void confirm();
    }

    /**
     * 加载进度条回调函数
     */
    public interface ProgressCallBack{
        void callback(ShapeLoadingDialog dialog);
    }


    /**
     * 展示进度条
     * @param ctx
     * @param progressCallBack
     */
    public static void showProgress(Context ctx,ProgressCallBack progressCallBack){
        ShapeLoadingDialog shapeLoadingDialog=new ShapeLoadingDialog(ctx);
        shapeLoadingDialog.setCanceledOnTouchOutside(false);
        shapeLoadingDialog.setLoadingText("加载中...");
        shapeLoadingDialog.show();
        progressCallBack.callback(shapeLoadingDialog);
    }
}
