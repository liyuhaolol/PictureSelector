package com.yalantis.ucrop.util;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;

import java.lang.reflect.Method;

public class AndroidBarUtils {
    public final static int NO_NAVIGATION = 0;
    public final static int NORMAL_NAVIGATION = 1;
    public final static int GESTURE_NAVIGATION = 2;

    public static void autoFitStatusBar(Activity activity, int statusBarId){
        if (statusBarId > 0){
            View statusbar = activity.findViewById(statusBarId);
            int height = DensityUtil.getStatusBarHeight(activity);
            int viewHeight = statusbar.getMeasuredHeight();
            if (viewHeight != height){
                //高度发生改变
                ViewGroup.LayoutParams layoutParams = statusbar.getLayoutParams();
                layoutParams.height = DensityUtil.getStatusBarHeight(activity);
                statusbar.setLayoutParams(layoutParams);
            }
        }
    }

    public static void autoFitStatusBar(Activity activity, View statusBar){
        if (statusBar != null){
            int height = DensityUtil.getStatusBarHeight(activity);//重新获取状态栏高度
            int viewHeight = statusBar.getMeasuredHeight();
            if (viewHeight != height){
                //高度发生改变
                ViewGroup.LayoutParams layoutParams = statusBar.getLayoutParams();
                layoutParams.height = height;
                statusBar.setLayoutParams(layoutParams);
            }
        }
    }

    public static void autoFitNavBar(final Activity activity, int navigationBarId){
        if (navigationBarId > 0){
            final View navigationbar = activity.findViewById(navigationBarId);
            getNavigationBarHeight(activity, new OnNavHeightListener() {
                @Override
                public void getHeight(int height,int navbarType) {
                    int viewHeight = navigationbar.getMeasuredHeight();
                    if (viewHeight != height){
                        //高度发生改变
                        ViewGroup.LayoutParams layoutParams = navigationbar.getLayoutParams();
                        layoutParams.height = height;
                        navigationbar.setLayoutParams(layoutParams);
                    }
                }
            });
        }
    }

    public static void autoFitNavBar(final Activity activity, final View navigationBar){
        if (navigationBar != null){
            getNavigationBarHeight(activity, new OnNavHeightListener() {
                @Override
                public void getHeight(int height,int navbarType) {
                    int viewHeight = navigationBar.getMeasuredHeight();
                    if (viewHeight != height){
                        //高度发生改变
                        ViewGroup.LayoutParams layoutParams = navigationBar.getLayoutParams();
                        layoutParams.height = height;
                        navigationBar.setLayoutParams(layoutParams);
                    }
                }
            });
        }
    }

    public static void getNavigationBarHeight(final Activity activity, final OnNavHeightListener listener) {
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height","dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        boolean canDo = true;
        boolean hasMenuKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_MENU);
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);

        boolean doSecondPart = false;//是否执行第二种判断逻辑
        if(hasMenuKey || hasBackKey || hasHomeKey) {
            //存在物理按键
            boolean hasNavigationBar = false;//初始不存在导航栏
            //这里要再次判断是否存在虚拟按键，因为有的手机，没有物理按键，他也会判断存在物理按键
            int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
            if (id > 0) {
                hasNavigationBar = resources.getBoolean(id);
            }
            if (!hasNavigationBar){
                //第一种方式判断没有导航栏，继续判断
                try {
                    Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
                    Method m = systemPropertiesClass.getMethod("get", String.class);
                    String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
                    if ("1".equals(navBarOverride)) {
                        hasNavigationBar = false;
                    } else{
                        hasNavigationBar = true;
                    }
                } catch (Exception e) {
                }
            }

            if (hasNavigationBar){
                //判断存在导航栏，与最初的判断冲突
                doSecondPart = true;
            }else {
                //判断确实没有导航栏，将高度改为0
                height = 0;
            }
        }else {
            //没有物理按键
            doSecondPart = true;
        }

        if (doSecondPart){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                //Android8.0以上才存在所谓全面屏手势
                canDo = false;
                activity.getWindow().getDecorView().post(new Runnable() {
                    @Override
                    public void run() {
                        if (activity.getWindow().getDecorView().getRootWindowInsets() != null){
                            if (listener != null){
                                int height = activity.getWindow().getDecorView().getRootWindowInsets().getStableInsetBottom();
                                listener.getHeight(height,getNavBarType(activity,height));
                            }
                        }
                    }
                });
            }else {
                //进到这里，说明，判断没有物理key，还不是android8.0
                //进行可用区域判断，辅助判断，并不是太靠谱
                Point size = new Point();
                Point realSize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(size);
                activity.getWindowManager().getDefaultDisplay().getRealSize(realSize);
                if (realSize.equals(size)) {
                    //两个尺寸相等，说明没有导航栏
                    height = 0;
                }
            }
        }


        if (canDo && listener != null){
            listener.getHeight(height,getNavBarType(activity,height));
        }
    }

    private static int getNavBarType(Activity activity,float navHeight){
        //获取屏幕高像素
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        float deviceHeight = metrics.heightPixels;
        float result = navHeight / deviceHeight;
        if (result == 0){
            return NO_NAVIGATION;
        }else if (result > 0 && result <= 0.03){
            return GESTURE_NAVIGATION;
        }else {
            return NORMAL_NAVIGATION;
        }
    }

    public static boolean setStatusBarMode(Window window, boolean darkFont){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){//11
                try{
                    window.getDecorView().getWindowInsetsController().setSystemBarsAppearance(
                            darkFont? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS:0,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    );
                    return true;
                }catch (Exception e){
                    Log.w("LightModeException","WindowInsetsController is NULL");
                    return false;
                }
            }else {
                View view = window.getDecorView();
                int oldVis = view.getSystemUiVisibility();
                int newVis = oldVis;
                if (darkFont){
                    newVis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }else {
                    newVis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                if (newVis != oldVis) {
                    view.setSystemUiVisibility(newVis);
                }
                return true;
            }
        }
        Log.w("LightModeException","Failed to match above Android 6.0");
        return false;
    }

    public static boolean setNavBarMode(Window window, boolean darkFont){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){//8.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){//11'
                try{
                    window.getDecorView().getWindowInsetsController().setSystemBarsAppearance(
                            darkFont? WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS:0,
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    );
                    return true;
                }catch (Exception e){
                    Log.w("LightModeException","WindowInsetsController is NULL");
                    return false;
                }
            }else {
                setSystemUiVisibility(window.getDecorView(), View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR,darkFont);
                return true;
            }
        }
        Log.w("LightModeException","Failed to match above Android 8.0");
        return false;
    }

    public static void setSystemUiVisibility(View decorView,int visibility,boolean isAddVisibility){
        int oldVis = decorView.getSystemUiVisibility();
        int newVis = oldVis;
        if (isAddVisibility){
            newVis |= visibility;
        }else {
            newVis &= ~visibility;
        }
        if (newVis != oldVis) {
            decorView.setSystemUiVisibility(newVis);
        }
    }
}
