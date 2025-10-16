package com.newAi302.app.utils

import android.os.Build
import android.text.TextUtils

object DeviceDetector {

    // 华为设备制造商标识
    private const val HUAWEI_MANUFACTURER = "HUAWEI"
    private const val HONOR_MANUFACTURER = "HONOR" // 荣耀曾经是华为子品牌，可根据需求决定是否包含

    // 鸿蒙系统属性标识
    private const val HARMONY_OS_VERSION_PROP = "ro.harmony.os.version"
    private const val HARMONY_OS_API_LEVEL_PROP = "ro.harmony.os.api_level"
    private const val HARMONY_OS_RELEASE_TYPE_PROP = "ro.harmony.os.release_type"

    /**
     * 判断是否为华为设备（包括荣耀旧机型）
     */
    fun isHuaweiDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER
        return TextUtils.equals(manufacturer, HUAWEI_MANUFACTURER)
                //|| TextUtils.equals(manufacturer, HONOR_MANUFACTURER)
    }

    /**
     * 判断是否为鸿蒙系统
     */
    fun isHarmonyOS(): Boolean {
        return try {
            // 方法1：检查鸿蒙系统特有属性
            val harmonyVersion = getSystemProperty(HARMONY_OS_VERSION_PROP)
            !TextUtils.isEmpty(harmonyVersion)
        } catch (e: Exception) {
            try {
                // 方法2：检查是否存在鸿蒙特有类（适用于API级别较低的鸿蒙系统）
                Class.forName("ohos.system.version.SystemVersion")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
        }
    }

    /**
     * 获取鸿蒙系统版本号
     */
    fun getHarmonyOSVersion(): String {
        return try {
            getSystemProperty(HARMONY_OS_VERSION_PROP) ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    /**
     * 获取鸿蒙系统API级别
     */
    fun getHarmonyOSApiLevel(): Int {
        return try {
            val apiLevel = getSystemProperty(HARMONY_OS_API_LEVEL_PROP)
            apiLevel?.toIntOrNull() ?: -1
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * 获取系统属性（通过反射调用隐藏API）
     */
    private fun getSystemProperty(key: String): String? {
        return try {
            val clz = Class.forName("android.os.SystemProperties")
            val method = clz.getMethod("get", String::class.java)
            method.invoke(null, key) as? String
        } catch (e: Exception) {
            null
        }
    }
}