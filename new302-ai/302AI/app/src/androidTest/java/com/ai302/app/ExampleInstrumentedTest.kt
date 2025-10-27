package com.newAi302.app

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.security.SecureRandom
import java.util.Random
import kotlin.random.asKotlinRandom // 导入扩展函数


import kotlin.math.pow



/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    //private val random = SecureRandom()
    //private val random: Random = TODO() // 声明为 Random 类型，赋值 SecureRandom 实例
    private val random = Random() // 定义为 Random 类型
    private val charPool = ('0'..'9') + ('A'..'Z') + ('a'..'z')
    @Test
    fun useAppContext() {
        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("com.newAi302.app", appContext.packageName)
        // 生成一个符合规则的字符串
        val validString = generateValidString()
        println("生成的字符串: $validString")
        // 验证生成的字符串
        val isValid = isValidString(validString)
        println("验证结果: ${if (isValid) "符合规则" else "不符合规则"}")
        val isValid1 = isValidString("1fe8g6ey")
        println("验证结果: ${if (isValid1) "符合规则" else "不符合规则"}")
    }

    /**
     * 生成符合规则的八位字符串
     * 规则：字符串转换成十进制后，前八位数字之和等于后八位数字之和
     */
    fun generateValidString(): String {
        while (true) {
            val candidate = generateRandomString()
            if (isValidString(candidate)) {
                return candidate
            }
        }
    }

    /**
     * 生成随机的八位字符串（包含数字和字母）
     */
    private fun generateRandomString(): String {
        return (1..8)
            .map { charPool.random(random.asKotlinRandom()) }
            .joinToString("")
    }

    /**
     * 验证字符串是否符合规则
     */
    fun isValidString(input: String): Boolean {
        if (input.length != 8) return false

        // 将字符串转换为十进制
        val decimalValue = stringToDecimal(input)

        // 将十进制数字转换为字符串
        val decimalStr = decimalValue.toString()

        // 如果十进制数字不足16位，用0补齐
        val paddedDecimalStr = decimalStr.padStart(16, '0')

        // 截取前八位和后八位
        val firstEight = paddedDecimalStr.take(8)
        val lastEight = paddedDecimalStr.takeLast(8)

        // 计算前八位数字之和
        val sumFirstEight = firstEight.sumOf { it.toString().toInt() }

        // 计算后八位数字之和
        val sumLastEight = lastEight.sumOf { it.toString().toInt() }

        return sumFirstEight == sumLastEight
    }

    /**
     * 将八位字符串转换为十进制数字
     */
    private fun stringToDecimal(input: String): Long {
        var result = 0L
        val base = 62 // 数字+大写字母+小写字母共62个字符

        for (i in input.indices) {
            val charValue = when {
                input[i] in '0'..'9' -> input[i] - '0'
                input[i] in 'A'..'Z' -> input[i] - 'A' + 10
                input[i] in 'a'..'z' -> input[i] - 'a' + 36
                else -> 0
            }
            result += charValue * base.toDouble().pow(7 - i).toLong()
        }
        return result
    }

    /**
     * 计算字符串的数字之和
     */
    private fun calculateDigitSum(numberStr: String): Int {
        return numberStr.sumOf { it.toString().toInt() }
    }

}


