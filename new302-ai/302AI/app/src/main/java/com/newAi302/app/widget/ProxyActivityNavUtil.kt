package com.newAi302.app.widget

import android.app.Activity
import android.content.Intent
import com.newAi302.app.MainActivity
import com.newAi302.app.ui.login.ForgetPassWordEmailActivity
import com.newAi302.app.ui.login.ForgetPassWordPhoneActivity
import com.newAi302.app.ui.login.LoginOneActivity
import com.newAi302.app.ui.login.RegisterActivity


/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/17
 * desc   : 公共页跳转管理类
 * version: 1.0
 */
class ProxyActivityNavUtil {

    companion object {

        /**
         * 跳转到主页
         */
        fun navMain(activity: Activity) {
            val intent = Intent(activity, MainActivity::class.java)//MainActivity
            activity.startActivity(intent)
        }

        /**
         * 跳转到登录页
         */
        fun navLogin(activity: Activity) {
            val intent = Intent(activity, LoginOneActivity::class.java)
            activity.startActivity(intent)
        }

        /**
         * 跳转到注册页
         */
        fun navToRegister(activity: Activity) {
            val intent = Intent(activity, RegisterActivity::class.java)
            activity.startActivity(intent)
        }

        /**
         * 跳转到忘记密码页 - email
         */
        fun navToForgetPassWordEmail(activity: Activity) {
            val intent = Intent(activity, ForgetPassWordEmailActivity::class.java)
            activity.startActivity(intent)
        }

        /**
         * 跳转到忘记密码页 - phone
         */
        fun navToForgetPassWordPhone(activity: Activity) {
            val intent = Intent(activity, ForgetPassWordPhoneActivity::class.java)
            activity.startActivity(intent)
        }
    }
}