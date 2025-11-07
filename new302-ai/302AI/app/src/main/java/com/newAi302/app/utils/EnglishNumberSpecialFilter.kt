package com.newAi302.app.utils

import android.text.InputFilter
import android.text.Spanned

/**
 * 限制输入：仅允许英文、数字、常见特殊字符（禁止中文）
 * 可根据需求调整正则表达式中的特殊字符范围
 */
class EnglishNumberSpecialFilter : InputFilter {
    // 正则规则：允许 a-z、A-Z、0-9 及指定特殊字符（可自行添加/删除特殊字符）
    private val allowedRegex = Regex("[^a-zA-Z0-9!@#$%^&*()_+-=\\[\\]{}|;':\",./<>?]")

    override fun filter(
        source: CharSequence?,  // 新输入的字符
        start: Int,            // 新输入字符的起始索引
        end: Int,              // 新输入字符的结束索引
        dest: Spanned?,        // 已有文本
        dstart: Int,           // 已有文本的起始插入位置
        dend: Int              // 已有文本的结束插入位置
    ): CharSequence? {
        // 过滤掉所有不匹配正则规则的字符（即中文和非法特殊字符）
        source?.let {
            val filtered = allowedRegex.replace(it, "")
            // 若过滤后与原输入不同，说明有非法字符，返回过滤后的结果
            if (filtered != it) {
                return filtered
            }
        }
        // 允许合法字符输入
        return null
    }
}