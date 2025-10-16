package com.newAi302.app.ui.login

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.newAi302.app.R
import com.newAi302.app.base.BaseActivity
//import com.newAi302.app.util.LogUtils
import com.newAi302.app.utils.SP
import com.newAi302.app.utils.ThreadUtils
import com.newAi302.app.utils.base.WearData
import com.newAi302.app.widget.ProxyActivityNavUtil

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   : 欢迎页
 * version: 1.0
 */
class SplashActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        SP.getInstance()

        initView()
    }


    private fun initView() {
        //判断是否已经登录
        if (WearData.getInstance().isLogin) {//WearData.getInstance().isLogin
            Log.e("ceshi","ceshi 是否有就登录了==========")
            navMain()
        } else {
            navLogin()
            Log.e("ceshi","ceshi 是否有就登录了====没有登录======")
        }
    }

    //去主页
    private fun navMain() {
        ProxyActivityNavUtil.navMain(this@SplashActivity)
        ThreadUtils.runOnUiThreadDelayed({ finish() }, 1000)
    }

    //去登录
    private fun navLogin() {
        ProxyActivityNavUtil.navLogin(this@SplashActivity)

        ThreadUtils.runOnUiThreadDelayed({ finish() }, 2000)
    }
}