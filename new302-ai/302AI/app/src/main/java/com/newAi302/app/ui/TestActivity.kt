package com.newAi302.app.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.newAi302.app.adapter.MediaAdapter
import com.newAi302.app.data.MediaItem
import com.newAi302.app.databinding.ActivityTestBinding
import com.newAi302.app.http.ImageDownloadService
import com.newAi302.app.http.ImageDownloader
import com.newAi302.app.utils.ImageToGalleryUtil.saveToGalleryAction
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.io.File

class TestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestBinding
    private lateinit var mediaAdapter: MediaAdapter
    private val mediaItems = mutableListOf<MediaItem>()


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        this.requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 0)
//        this.requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_VIDEO), 0)
        // 合并权限请求，避免重复请求
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissions(permissions.toTypedArray(), 0)


        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)

        mediaAdapter = MediaAdapter(this,mediaItems) { mediaItem ->
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

        binding.recyclerView.adapter = mediaAdapter

        // 加载媒体文件
        loadMediaFiles()

    }

    override fun onResume() {
        super.onResume()
        val TARGET_IMAGE_URL = "https://file.302.ai/gpt/imgs/aabfb15e833816434749d72e9777f60d.png"
        val retrofit = Retrofit.Builder()
            .baseUrl("https://file.302.ai/") // 占位（因用@Url动态URL）
            .build()
        val downloadService = retrofit.create(ImageDownloadService::class.java)
        val imageDownloader = ImageDownloader(downloadService)
        //  lifecycleScope：与Activity生命周期绑定，避免内存泄漏
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. 下载图片（IO线程）
                val localUrl = withContext(Dispatchers.IO) {
                    imageDownloader.downloadImage(TARGET_IMAGE_URL, this@TestActivity)
                }
                Log.e("ceshi","下载的图片地址:$localUrl")

                if (localUrl.isNullOrEmpty()) {
//                                                tvStatus.text = "下载失败，请检查网络"
//                                                btnDownload.isEnabled = true
//                                                return@launch
                }

                // 2. 下载成功，插入Room数据库（IO线程）
                withContext(Dispatchers.IO) {
//                                                val imageEntity = ImageEntity(
//                                                    networkUrl = TARGET_IMAGE_URL,
//                                                    localUrl = localUrl
//                                                )
                    //db.imageDao().insertImage(imageEntity)
                }

                // 3. 更新UI（主线程）
//                                            tvStatus.text = "下载成功！本地路径：$localUrl"
//                                            btnViewImage.isEnabled = true // 启用查看按钮

                // 3. 加载本地图片（主线程）
                withContext(Dispatchers.Main) {

                    // Glide配置：圆角+占位图（提升体验）
                    val options = RequestOptions()
                        .transform(com.bumptech.glide.load.resource.bitmap.RoundedCorners(20)) // 20dp圆角
                        .placeholder(android.R.drawable.ic_menu_gallery) // 加载中占位图
                        .error(android.R.drawable.stat_notify_error) // 加载失败占位图

                    // 加载本地文件到ImageView
                    Glide.with(this@TestActivity)
                        .load(File(localUrl)) // 传入本地文件
                        .apply(options)
                        .into(binding.showImage)

                    saveToGalleryAction(localUrl!!,this@TestActivity, lifecycleScope)
                }

            } catch (e: Exception) {
//                                            tvStatus.text = "下载异常：${e.message}"
//                                            btnDownload.isEnabled = true
            }
        }
    }

    private fun loadMediaFiles() {
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
                    Toast.makeText(this@TestActivity, "加载媒体文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}