package com.newAi302.app.widget.utils

import android.content.Context
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlin.random.Random


/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/5/14
 * desc   : 公用Utils
 * version: 1.0
 */
object CommonUtils {

    /**
     * 随机生产10位数的随机数  注： 用于接口使用
     * @param 0123456789abcdefghijklmnopqrstuvwxyz
     * @param length 随机数的长度
     */
    fun generateRandomString(length: Int): String {
        // 包含所有数字和大小写字母的字符池
//        val charPool = ('0'..'9') + ('a'..'z') + ('A'..'Z')
        val charPool = ('0'..'9') + ('a'..'z')
        return (1..length).map {
            // 随机选择一个字符
            charPool[Random.nextInt(0, charPool.size)]
        }.joinToString("")
    }

    /**
     * 判断邮箱正则表达式
     */
    fun isValidEmail(email: String?): Boolean {
        val emailRegex =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
        return email?.matches(emailRegex.toRegex()) ?: false
    }


    /**
     * 判断国际区号 + 手机号码是否正常 正则表达式
     *
     * @param phoneNumber 输入的手机号码 如：+86 13800138000
     */
    fun isPhoneNumberValid(context: Context, phoneNumber: String, countryCode: String): Boolean {
//        LogUtils.e("ceshi 当前isPhoneNumberValid ===：", phoneNumber, countryCode)
        // 正则表达式，匹配国际区号+手机号的格式，例如+86 13800138000
        val phoneUtil = PhoneNumberUtil.createInstance(context)
        try {
            val mPhoneNumber = phoneUtil.parse(phoneNumber, countryCode)
            return phoneUtil.isValidNumber(mPhoneNumber)
        } catch (e: NumberParseException) {
            e.printStackTrace()
        }
        return false
    }
}