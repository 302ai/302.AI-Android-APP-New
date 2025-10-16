package com.newAi302.app.data

import android.net.Uri


data class MediaItem(
    val id: Long,
    val uri: Uri,
    val isVideo: Boolean,
    val duration: Int = 0, // 视频时长(毫秒)，图片为0
    var type: String = "复制"
)
