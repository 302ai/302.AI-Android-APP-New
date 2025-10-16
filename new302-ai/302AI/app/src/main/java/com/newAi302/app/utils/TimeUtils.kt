package com.newAi302.app.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * author :
 * e-mail :
 * time   : 2025/4/16
 * desc   :
 * version: 1.0
 */
object TimeUtils {

    /**
     * 获取当前时间的字符串表示，格式为 yyyy-MM-dd HH:mm:ss
     * @return 当前时间的字符串
     */
    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    /**
     * 获取当前年份
     * @return 当前年份
     */
    fun getCurrentYear(): Int {
        val dateFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate).toInt()
    }

    /**
     * 获取当前月份
     * @return 当前月份（1 - 12）
     */
    fun getCurrentMonth(): Int {
        val dateFormat = SimpleDateFormat("MM", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate).toInt()
    }

    /**
     * 获取当前日期
     * @return 当前日期
     */
    fun getCurrentDay(): Int {
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate).toInt()
    }

    /**
     * 获取当前小时
     * @return 当前小时
     */
    fun getCurrentHour(): Int {
        val dateFormat = SimpleDateFormat("HH", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate).toInt()
    }

    /**
     * 获取当前分钟
     * @return 当前分钟
     */
    fun getCurrentMinute(): Int {
        val dateFormat = SimpleDateFormat("mm", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate).toInt()
    }

    /**
     * 获取当前秒数
     * @return 当前秒数
     */
    fun getCurrentSecond(): Int {
        val dateFormat = SimpleDateFormat("ss", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate).toInt()
    }

    fun getTimeTag(time: String, nowTime: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return try {
            val date = sdf.parse(time)
            val date1 = sdf.parse(nowTime)

            val calendar = Calendar.getInstance()
            val calendar1 = Calendar.getInstance()

            calendar.time = date
            calendar1.time = date1

            // 比较年
            val year = calendar.get(Calendar.YEAR)
            val year1 = calendar1.get(Calendar.YEAR)
            if (year != year1) {
                return "更早"
            }

            // 比较月
            val month = calendar.get(Calendar.MONTH)
            val month1 = calendar1.get(Calendar.MONTH)
            if (month != month1) {
                return "更早"
            }

            // 比较日
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val day1 = calendar1.get(Calendar.DAY_OF_MONTH)

            when {
                day == day1 -> "今日"
                day - day1 == -1 -> "昨天"
                else -> "更早"
            }
        } catch (e: ParseException) {
            // 处理解析异常，返回默认值
            "未知"
        }
    }

    /**
     * 通过时间戳获取 "yyyy-MM-dd HH:mm:ss" 格式的时间
     * @param timestamp 时间戳（单位：毫秒，如 System.currentTimeMillis() 的返回值）
     * @param zoneId 时区（默认：系统默认时区，可指定如 ZoneId.of("Asia/Shanghai")）
     * @return 格式化后的时间字符串
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTimestampToDateTime(
        timestamp: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        // 1. 将时间戳转换为 Instant（时间戳是 UTC 时间，需关联时区）
        val instant = Instant.ofEpochMilli(timestamp)
        // 2. 定义格式化器（指定目标格式）
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        // 3. 关联时区并格式化
        return instant.atZone(zoneId).format(formatter)
    }

}