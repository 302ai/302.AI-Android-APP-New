package com.newAi302.app.utils.base;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.newAi302.app.utils.AppUtils;
import com.newAi302.app.module.IConfigProvide;
import com.newAi302.app.utils.LogUtils;
import com.newAi302.app.utils.SP;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/7
 * desc   : app环境管理工具
 * version: 1.0
 */
public class WearUtil {

    public static final int EVN_TEST = 0;          //测试环境
    public static final int EVN_PRE_PRODUCT = 1;   //预生产环境
    public static final int EVN_PRODUCT = 2;       //生产环境（正式环境）
    private static final String TAG = WearUtil.class.getSimpleName();
    private static final String SP_KEY_WEAR_EVN = "proxy_evn";
    public static int appVersionCode;
    public static String appVersion = "";
    //    public static String APP_SERVER_HTTPS = "https://test-api2.proxy302.com";
    public static String APP_SERVER_HTTPS = "";
    public static String APP_DATE_SERVER = "https://test-api2.proxy302.com";

    public static String APP_SERVER_HTTPS_HTML = "";

    public static String APP_TOY_SERVER = "";
    public static String LOG_SERVER_HTTPS = "";
    public static Application application;
    private static int myEvn = EVN_TEST;
    public static final String SIG_FUNC = "getSignature";

    public static ScheduledExecutorService mThreadPool;

    public static void init(Application app) {
        application = app;
        appVersion = AppUtils.getAppVersionName().replace("_dev", "");
        appVersionCode = AppUtils.getAppVersionCode();
        int defaultV = AppUtils.isAppDebug() ? EVN_TEST : EVN_PRODUCT;
//        myEvn = SP.getInstance().getInt(SP_KEY_WEAR_EVN, defaultV);

    }

    public static Application getApplication() {
        return application;
    }

    public static void switchEvn(int evn) {
        SP.getInstance().put(SP_KEY_WEAR_EVN, evn);
    }

    public static int getMyEvn() {
        return myEvn;
    }

  /*  public static boolean isDebugOrTest() {
        return isTestEvn() || isPreProductEvn() || AppUtils.isAppDebug() || isReleaseDev();
    }*/

    public static boolean isTestEvn() {
        return myEvn == EVN_TEST;
    }

    public static boolean isPreProductEvn() {
        return myEvn == EVN_PRE_PRODUCT;
    }

    public static boolean isProductEvn() {
        return myEvn == EVN_PRODUCT;
    }

//    public static boolean isReleaseDev() {
//        return AppUtils.getAppVersionName().contains("_dev");
//    }

    public static void initEvn(IConfigProvide provide) {
        //测试环境
//        provide.initTest();
//        provide.initTestHtml();

        //正式环境
        provide.initProduct();
        provide.initProductHtml();

        /*if (isTestEvn()) {
            provide.initTest();
        } else if (isProductEvn()) {
            provide.initProduct();
        } else {
            provide.initPreProduct();
        }*/
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static void send(Object o) {
        try {
            EventBus.getDefault().post(o);
        } catch (EventBusException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "EventBus postSticky cancel postSticky event exception : " + e.toString());
        }
    }

    public static void sendSticky(Object o) {
        try {
            EventBus.getDefault().postSticky(o);
        } catch (EventBusException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "EventBus postSticky cancel postSticky event exception : " + e.toString());
        }
    }

    public static boolean removeStickyEvent(Object o) {
        try {
            return EventBus.getDefault().removeStickyEvent(o);
        } catch (EventBusException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "EventBus postSticky cancel postSticky event exception : " + e.toString());
        }
        return false;
    }

    public static void removeStickyEvent(Class c) {
        try {
            EventBus.getDefault().removeStickyEvent(c);
        } catch (EventBusException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "EventBus postSticky cancel postSticky event exception : " + e.toString());
        }
    }

    public static void register(Object o) {
        try {
            EventBus.getDefault().register(o);
        } catch (EventBusException e) {
        }
    }

    public static void unregister(Object o) {
        EventBus.getDefault().unregister(o);
    }

    public static boolean isSystemNightMode(Context context) {
        try {
            return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        } catch (Exception ex) {
        }
        return false;
    }

 /*   public static boolean isDarkMode(Context context) {
        if (context == null) {
            return false;
        }
        int darkMode = SP.getInt("wear_theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (darkMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            return isSystemNightMode(context);
        } else {
            return darkMode == AppCompatDelegate.MODE_NIGHT_YES;
        }
    }*/
}
