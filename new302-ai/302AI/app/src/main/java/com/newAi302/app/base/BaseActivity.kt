package com.newAi302.app.base

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.newAi302.app.MyApplication
import com.newAi302.app.R
import com.newAi302.app.utils.ActivityManager
import com.newAi302.app.utils.LanguageUtil
import com.newAi302.app.utils.ThemeUtil

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/8/1
 * desc   :
 * version: 1.0
 */
open class BaseActivity: AppCompatActivity() {
    private var currentLanguage: String? = null
    lateinit var sharedPreferences: SharedPreferences
    private  var isFirst = true
    override fun onCreate(savedInstanceState: Bundle?) {
        val customizeTheme = ThemeUtil.getSavedTheme(this)
        // 初始化SharedPreferences
        //sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        Log.e("ceshi","获取的主题是:$customizeTheme")
        if (customizeTheme == ThemeUtil.THEME_LIGHT){
            ThemeUtil.changeTheme(this, ThemeUtil.THEME_LIGHT)
        }else if (customizeTheme == ThemeUtil.THEME_NIGHT){
            ThemeUtil.changeTheme(this, ThemeUtil.THEME_NIGHT)
        }else{
            ThemeUtil.changeTheme(this, ThemeUtil.THEME_FOLLOW_SYSTEM)
        }

        setTheme(R.style.Theme_Ai302)
        // 在 onCreate 前应用语言设置
        applyLanguage()
        // 将当前Activity添加到管理器
        ActivityManager.addActivity(this)
        super.onCreate(savedInstanceState)
        currentLanguage = LanguageUtil.getSavedLanguage(this)

    }



    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 配置变化时重新应用语言设置
        applyLanguage()
    }

    override fun attachBaseContext(newBase: Context) {
        // 每次创建Activity时都应用最新的语言设置
        val language = LanguageUtil.getSavedLanguage(newBase)
        super.attachBaseContext(LanguageUtil.applyLanguage(newBase, language))
    }



    private fun applyLanguage() {
        val language = LanguageUtil.getSavedLanguage(this)
        val context = LanguageUtil.applyLanguage(this, language)
        val resources = context.resources
        val configuration = resources.configuration
        val displayMetrics = resources.displayMetrics
        // 更新配置
        resources.updateConfiguration(configuration, displayMetrics)
    }
//    private fun applyLanguage() {
//        val language = LanguageUtil.getSavedLanguage(this)
//        // 使用新的context替换当前activity的resources
//        val context = LanguageUtil.applyLanguage(this, language)
//        val resources = context.resources
//        val configuration = resources.configuration
//        val displayMetrics = resources.displayMetrics
//
//        // 更新当前activity的资源配置
//        @Suppress("DEPRECATION")
//        resources.updateConfiguration(configuration, displayMetrics)
//    }

    /**
     * 切换语言并重启Activity
     */
    protected fun switchLanguage(language: String) {
        LanguageUtil.saveLanguageSetting(this, language)
        // 重启当前Activity使语言设置生效
        recreate()
    }
    /**
     * 切换语言并重启应用
     */
//    protected fun switchLanguage(language: String) {
//        if (currentLanguage == language) {
//            return // 如果切换的是当前语言，直接返回
//        }
//
//        LanguageUtil.saveLanguageSetting(this, language)
//
//        // 更彻底的重启方式：清除栈并重启 launcher activity
//        val intent = packageManager.getLaunchIntentForPackage(packageName)
//        intent?.addFlags(
//            Intent.FLAG_ACTIVITY_CLEAR_TOP or
//                Intent.FLAG_ACTIVITY_CLEAR_TASK or
//                Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(intent)
//        finish()
//        // 添加过渡动画
//        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
//    }

    override fun onDestroy() {
        super.onDestroy()
        // 将当前Activity从管理器中移除
        ActivityManager.removeActivity(this)
    }

}