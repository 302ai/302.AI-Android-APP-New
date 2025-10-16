package com.newAi302.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.service.voice.VoiceInteractionSession.ActivityId
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.newAi302.app.MainActivity
import com.newAi302.app.MyApplication
import com.newAi302.app.R
import com.newAi302.app.databinding.ActivityImageViewerBinding
import com.newAi302.app.datastore.ImageUrlMapper
import com.newAi302.app.room.ChatDatabase
import com.newAi302.app.room.ChatItemRoom
import com.newAi302.app.utils.DialogUtils
import com.newAi302.app.utils.StringObjectUtils
import com.newAi302.app.utils.ViewAnimationUtils
import com.bumptech.glide.Glide
import com.newAi302.app.utils.ImageToGalleryUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageViewerActivity : AppCompatActivity() {
    private lateinit var binding:ActivityImageViewerBinding
    private var scale = 1.0f
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    private lateinit var dialogUtils: DialogUtils
    private val urlMapper = ImageUrlMapper(MyApplication.myApplicationContext)
    private lateinit var chatDatabase: ChatDatabase
    private var chatListSearch = mutableListOf<ChatItemRoom>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 初始化数据库
        chatDatabase = ChatDatabase.getInstance(this)

        binding.backImage.setOnClickListener {
            finish()
        }

        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
        // 获取图片URI并显示
        val imageUri = intent.getParcelableExtra<Uri>("IMAGE_URI")
        Glide.with(this)
            .load(imageUri)
            .into(binding.imageView)

        binding.moreImageLine.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            val options = mutableListOf("定位到聊天位置","复制","分享","保存到相册","上传到档案库","添加到知识库")
            dialogUtils.setupPopupWindow(options,"ResourceActivity",this@ImageViewerActivity)
            dialogUtils.showPopup(it)
        }

        dialogUtils = DialogUtils {
            Log.e("ceshi","弹窗返回$it")
            when(it){
                "定位到聊天位置" -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val localUrl = urlMapper.getNetworkUrl(StringObjectUtils.extractImageId(imageUri.toString()))
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
                            val intent = Intent(this@ImageViewerActivity, MainActivity::class.java)
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
                        val localUrl = urlMapper.getNetworkUrl(StringObjectUtils.extractImageId(imageUri.toString()))
                        Log.e("ceshi","网络URL:$localUrl")
                        if (localUrl != null){
                            ImageToGalleryUtil.saveToGalleryAction(
                                localUrl,
                                this@ImageViewerActivity,
                                lifecycleScope
                            )
                        }
                    }

                }
                "上传到档案库" -> {

                }
                "添加到知识库" -> {

                }


            }
        }


    }

    // 处理缩放手势
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scale *= detector.scaleFactor
            scale = Math.max(0.1f, Math.min(scale, 5.0f)) // 限制缩放范围
            binding.imageView.scaleX = scale
            binding.imageView.scaleY = scale
            return true
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }




}