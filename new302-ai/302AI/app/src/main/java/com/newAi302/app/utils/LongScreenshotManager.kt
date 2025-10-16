package com.newAi302.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent

class LongScreenshotManager(private val activity: Activity) {
    private val TAG = "LongScreenshotManager"
    private val REQUEST_MEDIA_PROJECTION = 1001

    // 获取媒体投影管理器
    private val mediaProjectionManager: MediaProjectionManager
        get() = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    /**
     * 启动长截图流程
     * 需要先请求屏幕捕获权限
     */
    fun startLongScreenshot() {
        // 检查Android版本，长截图功能在Android 11 (API 30)及以上支持更好
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Log.w(TAG, "长截图功能在Android 11及以上版本支持更好")
        }

        // 请求屏幕捕获权限
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        activity.startActivityForResult(intent, REQUEST_MEDIA_PROJECTION)
    }

    /**
     * 处理权限请求结果
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // 权限获取成功，启动长截图
                handleLongScreenshot(resultCode, data)
                return true
            } else {
                Log.e(TAG, "用户拒绝了屏幕捕获权限")
            }
        }
        return false
    }

    /**
     * 处理长截图逻辑
     */
    private fun handleLongScreenshot(resultCode: Int, data: Intent) {
        try {
            // 获取媒体投影
            val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)

            // 对于长截图，不同厂商可能有不同的实现方式
            // 这里使用通用的方式，发送长截图广播
            val screenshotIntent = Intent("com.android.systemui.screenshot.TAKE_SCREENSHOT")
            screenshotIntent.putExtra("extra_type", "long_screenshot")
            //screenshotIntent.putExtra("android.intent.extra.KEY_EVENT", null)
            screenshotIntent.putExtra(Intent.EXTRA_KEY_EVENT, null as KeyEvent?)

            // 某些设备可能需要额外参数
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                screenshotIntent.putExtra("extra_fullscreen", true)
            }

            activity.sendBroadcast(screenshotIntent)

            // 释放媒体投影资源
            mediaProjection?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "长截图处理失败: ${e.message}", e)
        }
    }
}
