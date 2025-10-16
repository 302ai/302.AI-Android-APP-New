package com.newAi302.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.*

object LanguageUtil {
    // 语言常量
    const val LANGUAGE_EN = "en" // 英文
    const val LANGUAGE_ZH = "zh" // 中文
    const val LANGUAGE_JA = "ja" // 日文

    /**
     * 应用语言设置
     */
    /*fun applyLanguage(context: Context, language: String): Context {
        val resources = context.resources
        val configuration = resources.configuration
        val locale = when (language) {
            LANGUAGE_ZH -> Locale.SIMPLIFIED_CHINESE
            LANGUAGE_JA -> Locale.JAPANESE
            else -> Locale.ENGLISH
        }

        // 根据系统版本设置语言
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            configuration.locales.set(0, locale)
            configuration.locales
            return context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
            return context
        }*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 1. 创建包含目标 Locale 的 LocaleList
            val localeList = LocaleList(locale)
            // 2. 通过 setLocales 方法设置语言列表
            configuration.setLocales(localeList)
            LocaleList.setDefault(localeList)
            // 3. 创建新的上下文并返回
            return context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
            return context
        }
    }*/
    fun applyLanguage(context: Context, language: String): Context {
        val resources = context.resources
        val configuration = resources.configuration
        val locale = when (language) {
            LANGUAGE_ZH -> Locale.SIMPLIFIED_CHINESE
            LANGUAGE_JA -> Locale.JAPANESE
            else -> Locale.ENGLISH
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 高版本：使用LocaleList设置
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
            context.createConfigurationContext(configuration)
        } else {
            // 低版本：直接修改locale（已废弃，但仍可用）
            @Suppress("DEPRECATION")
            configuration.locale = locale
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
            context
        }
    }

    /**
     * 为 singleTask Activity 应用语言（兼容高版本）
     */
    fun applyLanguageToActivity(activity: Activity, language: String) {
        val locale = when (language) {
            LANGUAGE_ZH -> Locale.SIMPLIFIED_CHINESE
            LANGUAGE_JA -> Locale.JAPANESE
            else -> Locale.ENGLISH
        }

        // 1. 创建新的 Configuration（高版本不可变，必须新建）
        // 替代 clone() 方法的方式
        val newConfig = Configuration(activity.resources.configuration)

        // 2. 设置新的语言
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            newConfig.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            newConfig.locale = locale
        }

        // 3. 关键：通过 Activity 的 Context 重新获取资源，并替换旧资源
        val displayMetrics = activity.resources.displayMetrics
        // 高版本用 createConfigurationContext 创建新上下文，低版本用 updateConfiguration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            activity.createConfigurationContext(newConfig)
        } else {
            @Suppress("DEPRECATION")
            activity.resources.updateConfiguration(newConfig, displayMetrics)
        }

        // 4. 强制刷新资源缓存（关键步骤）
        try {
            val resources = activity.resources
            val field = resources.javaClass.getDeclaredField("mResourcesImpl")
            field.isAccessible = true
            field.set(resources, null) // 清除资源实现缓存
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 5. 保存语言设置
        saveLanguageSetting(activity, language)
    }

    /**
     * 保存用户选择的语言
     */
    fun saveLanguageSetting(context: Context, language: String) {
        val sp = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        sp.edit().putString("selected_language", language).apply()
    }

    /**
     * 获取保存的语言设置
     */
    fun getSavedLanguage(context: Context): String {
        val sp = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return sp.getString("selected_language", LANGUAGE_ZH) ?: LANGUAGE_ZH
    }

    /**
     * 切换语言并重启应用
     */
    fun changeLanguage(context: Context, language: String) {
        // 保存语言设置
        saveLanguageSetting(context, language)

        // 重启应用的主Activity
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

        // 杀死当前进程，确保所有资源都被重新加载
        android.os.Process.killProcess(android.os.Process.myPid())
    }

}
