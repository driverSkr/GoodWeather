package com.driverskr.goodweather.utils;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.driverskr.goodweather.R;

/**
 * @Author: driverSkr
 * @Time: 2023/11/13 18:21
 * @Description: 弹窗显示方式$
 */
public class DialogShowStyle {

    private DialogShowStyle mDialogShowStyle;
    private PopupWindow mPopupWindow;
    private LayoutInflater inflater;
    private Context mContext;

    public DialogShowStyle(Context context) {
        this.mContext = context;
        inflater = LayoutInflater.from(context);
        mDialogShowStyle = this;
    }

    /**
     * 中间显示
     * @param mView 弹窗
     */
    public void showCenterPopupWindow(View mView, int width, int height, boolean focusable) {
        mPopupWindow = new PopupWindow(mView, width, height, focusable);
        mPopupWindow.setContentView(mView);
        //设置动画
        mPopupWindow.setAnimationStyle(R.style.AnimationCenterFade);
        mPopupWindow.showAtLocation(mView, Gravity.CENTER, 0, 0);
        mPopupWindow.update();
        setBackgroundAlpha(0.5f,mContext);
        WindowManager.LayoutParams normal = ((Activity) mContext).getWindow().getAttributes();
        normal.alpha = 0.5f;
        ((Activity) mContext).getWindow().setAttributes(normal);
        mPopupWindow.setOnDismissListener(closeDismiss);
    }

    public static void setBackgroundAlpha(float bgAlpha,Context mContext){
        WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
        lp.alpha = bgAlpha;
        ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        ((Activity) mContext).getWindow().setAttributes(lp);
    }

    /**
     * 设置弹窗动画
     */
    public DialogShowStyle setAnim(int animId) {
        if (mPopupWindow != null) {
            mPopupWindow.setAnimationStyle(animId);
        }
        return mDialogShowStyle;
    }

    //弹窗消失时关闭阴影
    public PopupWindow.OnDismissListener closeDismiss = () -> {
        WindowManager.LayoutParams normal = ((Activity) mContext).getWindow().getAttributes();
        normal.alpha = 1f;
        ((Activity) mContext).getWindow().setAttributes(normal);
    };
}
