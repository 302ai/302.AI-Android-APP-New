package com.newAi302.app.utils

import android.view.View
import android.view.ViewTreeObserver
import android.util.Log
import com.newAi302.app.data.ScreenPosition

object ViewScreenPositionHelper {
    private const val TAG = "ScreenPositionHelper"

    /**
     * 直接测量 View 在屏幕上的绝对位置
     * @param targetView 要测量的目标 View（必须属于当前屏幕的视图树）
     * @param onResult 测量结果回调（返回 ScreenPosition，含 X、Y 坐标）
     * @param onError 测量失败回调（可选，返回失败原因）
     */
    fun getViewScreenPosition(
        targetView: View,
        onResult: (ScreenPosition) -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        // 第一步：检查 View 有效性（避免无效测量）
        val errorMsg = checkViewValid(targetView)
        if (errorMsg != null) {
            onError?.invoke(errorMsg)
            Log.e(TAG, "测量失败：$errorMsg")
            return
        }

        // 第二步：确保 View 布局完成后再测量（核心避坑）
        measureAfterLayout(targetView) {
            // 第三步：调用系统方法获取屏幕坐标
            val location = IntArray(2)
            targetView.getLocationOnScreen(location) // 关键方法！
            val screenX = location[0]
            val screenY = location[1]

            // 回调结果
            onResult(ScreenPosition(screenX, screenY))
            Log.d(TAG, "测量成功：屏幕位置 X=$screenX, Y=$screenY")
        }
    }

    /**
     * 检查 View 是否具备测量条件（避坑关键）
     * @return 失败原因（null 表示有效）
     */
    private fun checkViewValid(view: View): String? {
        return when {
            !view.isAttachedToWindow -> "View 未附加到屏幕窗口（isAttachedToWindow = false）"
            view.visibility != View.VISIBLE -> "View 不可见（visibility = ${view.visibility}，需设为 VISIBLE）"
            view.layoutParams == null -> "View 未设置布局参数（layoutParams = null）"
            else -> null
        }
    }

    /**
     * 布局完成后再测量（比 View.post() 更稳定）
     */
    private fun measureAfterLayout(view: View, action: () -> Unit) {
        // 已布局完成（宽高 > 0），直接执行
        if (view.width > 0 && view.height > 0) {
            action()
            return
        }

        // 用 OnPreDrawListener 监听（绘制前触发，布局完全稳定）
        val preDrawListener = object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                // 移除监听，避免多次触发
                view.viewTreeObserver.removeOnPreDrawListener(this)
                // 再次检查有效性（防止测量中 View 被移除）
                if (checkViewValid(view) == null) {
                    action()
                }
                return true // 不影响 View 正常绘制
            }
        }

        // 安全添加监听（避免 View 已销毁导致空指针）
        if (view.viewTreeObserver.isAlive) {
            view.viewTreeObserver.addOnPreDrawListener(preDrawListener)
        } else {
            // 兜底方案：延迟 100ms（足够布局完成）
            view.postDelayed({
                if (checkViewValid(view) == null) action()
            }, 100)
        }
    }
}