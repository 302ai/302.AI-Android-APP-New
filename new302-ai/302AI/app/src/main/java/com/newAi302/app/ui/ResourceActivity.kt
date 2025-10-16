package com.newAi302.app.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.newAi302.app.MainActivity
import com.newAi302.app.MyApplication.Companion.myApplicationContext
import com.newAi302.app.R
import com.newAi302.app.adapter.MediaAdapter
import com.newAi302.app.base.BaseActivity
import com.newAi302.app.data.MediaItem
import com.newAi302.app.databinding.ActivityResourceBinding
import com.newAi302.app.datastore.ImageUrlMapper
import com.newAi302.app.room.ChatDatabase
import com.newAi302.app.room.ChatItemRoom
import com.newAi302.app.utils.ImageToGalleryUtil
import com.newAi302.app.utils.StringObjectUtils
import com.newAi302.app.utils.SystemUtils
import com.newAi302.app.utils.TimeUtils
import com.newAi302.app.utils.TimeUtils.formatTimestampToDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.ZoneId

class ResourceActivity : BaseActivity() {
    private lateinit var binding: ActivityResourceBinding
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var mediaYesterdayAdapter: MediaAdapter
    private lateinit var mediaEarlierAdapter: MediaAdapter
    private val mediaItems = mutableListOf<MediaItem>()
    private val mediaYesterdayItems = mutableListOf<MediaItem>()
    private val mediaEarlierItems = mutableListOf<MediaItem>()
    private val urlMapper = ImageUrlMapper(myApplicationContext)
    private lateinit var chatDatabase: ChatDatabase
    private var chatListSearch = mutableListOf<ChatItemRoom>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityResourceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 初始化数据库
        chatDatabase = ChatDatabase.getInstance(this)

        // 合并权限请求，避免重复请求
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissions(permissions.toTypedArray(), 0)

        binding.backImage.setOnClickListener {
            finish()
        }


        binding.recyclerView1.layoutManager = GridLayoutManager(this, 5)
        binding.recyclerView2.layoutManager = GridLayoutManager(this, 5)
        binding.recyclerView3.layoutManager = GridLayoutManager(this, 5)

        mediaAdapter = MediaAdapter(this,mediaItems) { mediaItem ->
            Log.e("ceshi","点击资源返回数据：$mediaItem")
            when(mediaItem.type){
                "定位到聊天位置" -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val localUrl = urlMapper.getNetworkUrl(StringObjectUtils.extractImageId(mediaItem.uri.toString()))
                        Log.e("ceshi","网络URL:$localUrl")
                        if (localUrl != null){
                            chatListSearch = chatDatabase.chatDao().getChatsWithMessageContaining(localUrl.toString()).toMutableList()
                            Log.e("ceshi","查找到的数据是：${chatListSearch[0]}")

                            //val allIndices = chatListSearch[0].messages.indices.filter { chatListSearch[0].messages[it].message == localUrl }
                            // 2. 从 ChatItemRoom 的 messages 列表中，查找包含目标链接的 ChatMessage 下标
                            val targetIndex = chatListSearch[0].messages.indexOfFirst { chatMessage ->
                                // 核心：用 contains() 检查 message 字段是否包含目标链接（而非完全相等）
                                chatMessage.message.contains(localUrl)
                            }
                            Log.e("ceshi","定位到的位置${targetIndex}")
                            val intent = Intent(this@ResourceActivity, MainActivity::class.java)
                            intent.putExtra("chat_item", chatListSearch[0])
                            if (targetIndex >=0 ){
                                intent.putExtra("chat_position",targetIndex)
                            }

                            startActivity(intent)
                            finish()
                        }
                    }


                }
                "复制" -> {

                }
                "分享" -> {

                }
                "保存到相册" -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val localUrl = urlMapper.getNetworkUrl(StringObjectUtils.extractImageId(mediaItem.uri.toString()))
                        Log.e("ceshi","网络URL:$localUrl")
                        if (localUrl != null){
                            ImageToGalleryUtil.saveToGalleryAction(
                                mediaItem.uri.toString(),
                                this@ResourceActivity,
                                lifecycleScope
                            )
                        }
                    }



                }
                "上传到档案库" -> {

                }
                "添加到知识库" -> {

                }
                "查看" -> {
                    if (mediaItem.isVideo) {
                        val intent = Intent(this, VideoPlayerActivity::class.java)
                        intent.putExtra("VIDEO_URI", mediaItem.uri)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, ImageViewerActivity::class.java)
                        intent.putExtra("IMAGE_URI", mediaItem.uri)
                        startActivity(intent)
                    }
                }

            }


        }

        mediaYesterdayAdapter = MediaAdapter(this,mediaYesterdayItems) { mediaItem ->
            Log.e("ceshi","点击资源返回数据：$mediaItem")
            when(mediaItem.type){
                "定位到聊天位置" -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val localUrl = urlMapper.getNetworkUrl(StringObjectUtils.extractImageId(mediaItem.uri.toString()))
                        Log.e("ceshi","网络URL:$localUrl")
                        if (localUrl != null){
                            chatListSearch = chatDatabase.chatDao().getChatsWithMessageContaining(localUrl.toString()).toMutableList()
                            Log.e("ceshi","查找到的数据是：${chatListSearch[0]}")

                            //val allIndices = chatListSearch[0].messages.indices.filter { chatListSearch[0].messages[it].message == localUrl }
                            // 2. 从 ChatItemRoom 的 messages 列表中，查找包含目标链接的 ChatMessage 下标
                            val targetIndex = chatListSearch[0].messages.indexOfFirst { chatMessage ->
                                // 核心：用 contains() 检查 message 字段是否包含目标链接（而非完全相等）
                                chatMessage.message.contains(localUrl)
                            }
                            Log.e("ceshi","定位到的位置${targetIndex}")
                            val intent = Intent(this@ResourceActivity, MainActivity::class.java)
                            intent.putExtra("chat_item", chatListSearch[0])
                            if (targetIndex >=0 ){
                                intent.putExtra("chat_position",targetIndex)
                            }

                            startActivity(intent)
                            finish()
                        }
                    }


                }
                "复制" -> {

                }
                "分享" -> {

                }
                "保存到相册" -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val localUrl = urlMapper.getNetworkUrl(StringObjectUtils.extractImageId(mediaItem.uri.toString()))
                        Log.e("ceshi","网络URL:$localUrl")
                        if (localUrl != null){
                            ImageToGalleryUtil.saveToGalleryAction(
                                mediaItem.uri.toString(),
                                this@ResourceActivity,
                                lifecycleScope
                            )
                        }
                    }
                }
                "上传到档案库" -> {

                }
                "添加到知识库" -> {

                }
                "查看" -> {
                    if (mediaItem.isVideo) {
                        val intent = Intent(this, VideoPlayerActivity::class.java)
                        intent.putExtra("VIDEO_URI", mediaItem.uri)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, ImageViewerActivity::class.java)
                        intent.putExtra("IMAGE_URI", mediaItem.uri)
                        startActivity(intent)
                    }
                }

            }


        }

        mediaEarlierAdapter = MediaAdapter(this,mediaEarlierItems) { mediaItem ->
            Log.e("ceshi","点击资源返回数据：$mediaItem")
            when(mediaItem.type){
                "定位到聊天位置" -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val localUrl = urlMapper.getNetworkUrl(StringObjectUtils.extractImageId(mediaItem.uri.toString()))
                        Log.e("ceshi","网络URL:$localUrl")
                        if (localUrl != null){
                            chatListSearch = chatDatabase.chatDao().getChatsWithMessageContaining(localUrl.toString()).toMutableList()
                            Log.e("ceshi","查找到的数据是：${chatListSearch[0]}")

                            //val allIndices = chatListSearch[0].messages.indices.filter { chatListSearch[0].messages[it].message == localUrl }
                            // 2. 从 ChatItemRoom 的 messages 列表中，查找包含目标链接的 ChatMessage 下标
                            val targetIndex = chatListSearch[0].messages.indexOfFirst { chatMessage ->
                                // 核心：用 contains() 检查 message 字段是否包含目标链接（而非完全相等）
                                chatMessage.message.contains(localUrl)
                            }
                            Log.e("ceshi","定位到的位置${targetIndex}")
                            val intent = Intent(this@ResourceActivity, MainActivity::class.java)
                            intent.putExtra("chat_item", chatListSearch[0])
                            if (targetIndex >=0 ){
                                intent.putExtra("chat_position",targetIndex)
                            }

                            startActivity(intent)
                            finish()
                        }
                    }


                }
                "复制" -> {

                }
                "分享" -> {

                }
                "保存到相册" -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val localUrl = urlMapper.getNetworkUrl(StringObjectUtils.extractImageId(mediaItem.uri.toString()))
                        Log.e("ceshi","网络URL:$localUrl")
                        if (localUrl != null){
                            ImageToGalleryUtil.saveToGalleryAction(
                                mediaItem.uri.toString(),
                                this@ResourceActivity,
                                lifecycleScope
                            )
                        }
                    }
                }
                "上传到档案库" -> {

                }
                "添加到知识库" -> {

                }
                "查看" -> {
                    if (mediaItem.isVideo) {
                        val intent = Intent(this, VideoPlayerActivity::class.java)
                        intent.putExtra("VIDEO_URI", mediaItem.uri)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, ImageViewerActivity::class.java)
                        intent.putExtra("IMAGE_URI", mediaItem.uri)
                        startActivity(intent)
                    }
                }

            }


        }

        binding.recyclerView1.adapter = mediaAdapter
        binding.recyclerView2.adapter = mediaYesterdayAdapter
        binding.recyclerView3.adapter = mediaEarlierAdapter

        // 加载媒体文件
        //loadMediaFiles()
        //加载资源库图片视频
        loadPrivateImages()

    }


    private fun loadMediaFiles() {
        binding.const1.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 查询图片
                val imageCursor = contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA),
                    null,
                    null,
                    "${MediaStore.Images.Media.DATE_ADDED} DESC"
                )

                // 查询视频
                val videoCursor = contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.DATA,
                        MediaStore.Video.Media.DURATION
                    ),
                    null,
                    null,
                    "${MediaStore.Video.Media.DATE_ADDED} DESC"
                )

                // 处理图片
                imageCursor?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                            .appendPath(id.toString())
                            .build()

                        mediaItems.add(MediaItem(id, uri, false))
                    }
                }

                // 处理视频
                var videoCount = 0
                videoCursor?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val duration = cursor.getInt(durationColumn)
                        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI.buildUpon()
                            .appendPath(id.toString())
                            .build()

                        mediaItems.add(MediaItem(id, uri, true, duration))
                        videoCount++
                    }
                }
                Log.d("MediaLoad", "加载视频数量: $videoCount")

                // 按日期排序（最新的在前）
                mediaItems.sortByDescending { it.id }

                // 更新UI
                withContext(Dispatchers.Main) {
                    mediaAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ResourceActivity, "加载媒体文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // 替换原来的图片查询逻辑，使用这个方法
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadPrivateImages() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 获取应用内部存储的images目录
                val imagesDir = File(filesDir, "images")

                // 检查目录是否存在
                if (!imagesDir.exists() || !imagesDir.isDirectory) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ResourceActivity, "图片目录不存在", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // 定义支持的图片格式
                val imageExtensions = arrayOf(".jpg", ".jpeg", ".png", ".gif", ".webp")

                // 遍历目录下的所有文件
                val imageFiles = imagesDir.listFiles { file ->
                    // 只保留图片文件
                    imageExtensions.any { ext ->
                        file.name.lowercase().endsWith(ext)
                    }
                }

                // 处理查询到的图片文件
                imageFiles?.forEach { file ->
                    // 生成文件的Uri
                    val uri = FileProvider.getUriForFile(
                        this@ResourceActivity,
                        "${packageName}.fileprovider", // 需要在Manifest中配置
                        file
                    )

                    // 获取文件最后修改时间作为排序依据
                    val lastModified = file.lastModified()
                    Log.e("ceshi","图片时间$lastModified")
                    /*Log.e("ceshi","图片的URL是：${SystemUtils.uriToTempFile(this@ResourceActivity, uri).path}")
                    val localUrl = urlMapper.getNetworkUrl(StringObjectUtils.extractImageId(uri.toString()))
                    Log.e("ceshi","网络URL:$localUrl")
                    if (localUrl != null){
                        lifecycleScope.launch(Dispatchers.IO) {
                            chatListSearch = chatDatabase.chatDao().getChatsWithMessageContaining(localUrl.toString()).toMutableList()
                            Log.e("ceshi","查找到的数据是：${chatListSearch[0]}")
                        }
                    }*/
                    val time = formatTimestampToDateTime(lastModified, ZoneId.of("Asia/Shanghai"))
                    Log.e("ceshi","图片时间正常${formatTimestampToDateTime(lastModified, ZoneId.of("Asia/Shanghai"))}")
                    val timeTag = TimeUtils.getTimeTag(time,TimeUtils.getCurrentDateTime())
                    lifecycleScope.launch(Dispatchers.Main) {
                        when(timeTag){
                            "今日" -> {
                                binding.const1.visibility = View.VISIBLE
                                mediaItems.add(MediaItem(lastModified, uri, false))

                            }
                            "昨天" -> {
                                binding.const2.visibility = View.VISIBLE
                                mediaYesterdayItems.add(MediaItem(lastModified, uri, false))
                            }
                            "更早" -> {
                                binding.const3.visibility = View.VISIBLE
                                mediaEarlierItems.add(MediaItem(lastModified, uri, false))
                            }
                        }
                    }

                }

                // 按修改时间排序（最新的在前）
                mediaItems.sortByDescending { it.id }
                mediaYesterdayItems.sortByDescending { it.id }
                mediaEarlierItems.sortByDescending { it.id }

                // 更新UI
                withContext(Dispatchers.Main) {
                    mediaAdapter.notifyDataSetChanged()
                    mediaYesterdayAdapter.notifyDataSetChanged()
                    mediaEarlierAdapter.notifyDataSetChanged()
                    Toast.makeText(
                        this@ResourceActivity,
                        "加载了 ${imageFiles?.size ?: 0} 张图片",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ResourceActivity, "加载图片失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



}