package com.newAi302.app.screenshot.model.config

/**
 * description:适配高版本Android系统静态变量，以及相关方法
 * author: bear .
 * Created date:  2019-06-25.
 * mail:2280885690@qq.com
 */
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionConst {
    companion object {
        val REQUEST_PERMISSION_STORY_WRITE = 1101//请求读权限

        val REQUEST_WRITE_CONSTACTS_PERMISSIONS = 1102//请求获取写入通讯录的权限
        val REQUEST_READ_PHONE_STATE_PERMISSIONS = 1103//请求获取写入通讯录的权限

        val REQUEST_PERMISSION_STORAGE = 1101 // 统一用一个请求码处理存储相关权限

        fun canUseThisPermission(
            mActivity: Activity,
            permissionName: String,
            permission: String,
            request: Int
        ): Boolean {
            val permissionCheck = ContextCompat.checkSelfPermission(mActivity, permission)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                    if (!mActivity.isFinishing) {
                        val builder = AlertDialog.Builder(mActivity)
                        builder.setTitle("权限设置")
                            .setMessage(
                                "请开通" + permissionName +
                                        "权限再使用该功能！"
                            )
                            .setPositiveButton("开启") { dialog, _ ->
                                ActivityCompat.requestPermissions(mActivity, arrayOf(permission), request)
                                dialog.dismiss()
                            }
                            .setNegativeButton("取消", null).create().show()
                    }
                } else {
                    ActivityCompat.requestPermissions(mActivity, arrayOf(permission), request)
                }
                return false
            } else {
                return true
            }
        }


        /**
         * 动态判断并请求存储权限（适配 Android 13+）
         * @param mActivity 上下文
         * @param mediaType 媒体类型："images"（图片）、"video"（视频）、"audio"（音频）
         */
        fun canUseStoragePermission(
            mActivity: Activity,
            mediaType: String = "images" // 默认处理图片权限
        ): Boolean {
            // 1. 根据 Android 版本选择要请求的权限
            val targetPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+（API 33+）：使用新的媒体权限
                when (mediaType) {
                    "images" -> Manifest.permission.READ_MEDIA_IMAGES
                    "video" -> Manifest.permission.READ_MEDIA_VIDEO
                    "audio" -> Manifest.permission.READ_MEDIA_AUDIO
                    else -> Manifest.permission.READ_MEDIA_IMAGES
                }
            } else {
                // Android 12-（API < 33）：使用旧的存储权限
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            }

            // 2. 检查权限是否已授权
            val permissionCheck = ContextCompat.checkSelfPermission(mActivity, targetPermission)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                // 3. 需要申请权限：显示提示对话框或直接请求
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        mActivity,
                        targetPermission
                    )
                ) {
                    if (!mActivity.isFinishing) {
                        AlertDialog.Builder(mActivity)
                            .setTitle("权限设置")
                            .setMessage("请开通存储权限以使用该功能！")
                            .setPositiveButton("开启") { dialog, _ ->
                                // 发起权限请求
                                ActivityCompat.requestPermissions(
                                    mActivity,
                                    arrayOf(targetPermission),
                                    REQUEST_PERMISSION_STORAGE
                                )
                                dialog.dismiss()
                            }
                            .setNegativeButton("取消", null)
                            .create()
                            .show()
                    }
                } else {
                    // 直接请求权限
                    ActivityCompat.requestPermissions(
                        mActivity,
                        arrayOf(targetPermission),
                        REQUEST_PERMISSION_STORAGE
                    )
                }
                return false
            } else {
                // 权限已授权
                return true
            }
        }



    }
}
