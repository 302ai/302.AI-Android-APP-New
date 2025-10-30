package com.newAi302.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import java.util.*

object ThemeUtil {
    // 语言常量
    const val THEME_LIGHT = "light" //
    const val THEME_NIGHT = "night" //
    const val THEME_FOLLOW_SYSTEM = "follow_system" // 跟随系统模式

    // 保存当前主题的缓存（避免每次都重建）
    private var currentTheme: String = THEME_FOLLOW_SYSTEM

    // ======================== 新增：深色模式相关方法 ========================
    /**
     * 1. 保存用户选择的主题模式（到 SharedPreferences）
     */
    fun saveThemeSetting(context: Context, themeMode: String) {
        val sp = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        sp.edit().putString("selected_theme", themeMode).apply()
    }

    /**
     * 2. 获取保存的主题模式（默认：跟随系统）
     */
    fun getSavedTheme(context: Context): String {
        val sp = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return sp.getString("selected_theme", THEME_FOLLOW_SYSTEM) ?: THEME_FOLLOW_SYSTEM
    }



    /**
     * 3. 应用主题模式（核心：设置 AppCompatDelegate 模式）
     */
    fun applyTheme(context: Context) {
        val savedTheme = getSavedTheme(context)
        // 根据保存的主题模式，设置全局深色模式
        val nightMode = when (savedTheme) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO // 浅色
            THEME_NIGHT -> AppCompatDelegate.MODE_NIGHT_YES // 深色
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM // 跟随系统
        }
        // 应用主题（全局生效）
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    /**
     * 4. 切换主题模式（并重启当前Activity使生效）
     */
    fun changeTheme(activity: Activity, themeMode: String) {
        // 关键：如果目标主题与当前主题一致，直接返回，不做任何操作
        if (themeMode == currentTheme) {
            return
        }
        // 否则，更新缓存并重建 Activity
        currentTheme = themeMode
        // 1. 保存主题设置
        saveThemeSetting(activity, themeMode)
        // 2. 应用主题
        applyTheme(activity)
        // 3. 重建Activity，让主题立即生效（无需重启整个应用）
        activity.recreate()
    }

    /**
     * 5. 判断当前是否为深色模式（辅助方法：用于UI适配）
     */
    fun isDarkMode(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

}
