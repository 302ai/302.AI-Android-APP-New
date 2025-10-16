package com.newAi302.app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.newAi302.app.MainActivity
import com.newAi302.app.MyApplication
import com.newAi302.app.R
import com.newAi302.app.base.BaseActivity
import com.newAi302.app.databinding.ActivityImageViewerBinding
import com.newAi302.app.databinding.ActivityVideoPlayerBinding
import com.newAi302.app.datastore.ImageUrlMapper
import com.newAi302.app.room.ChatDatabase
import com.newAi302.app.room.ChatItemRoom
import com.newAi302.app.utils.DialogUtils
import com.newAi302.app.utils.ImageToGalleryUtil
import com.newAi302.app.utils.StringObjectUtils
import com.newAi302.app.utils.ViewAnimationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoPlayerActivity : BaseActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    private val handler = Handler(Looper.getMainLooper())
    private var progressUpdateRunnable: Runnable? = null

    private lateinit var dialogUtils: DialogUtils

    private val urlMapper = ImageUrlMapper(MyApplication.myApplicationContext)
    private lateinit var chatDatabase: ChatDatabase
    private var chatListSearch = mutableListOf<ChatItemRoom>()
    private var mVideoUri = ""

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 初始化数据库
        chatDatabase = ChatDatabase.getInstance(this)
        binding.backImage.setOnClickListener {
            finish()
        }

        binding.moreImageLine.setOnClickListener {
            ViewAnimationUtils.performClickEffect(it)
            val options = mutableListOf("定位到聊天位置","复制","分享","保存到相册","上传到档案库","添加到知识库")
            dialogUtils.setupPopupWindow(options,"ResourceActivity",this@VideoPlayerActivity)
            dialogUtils.showPopup(it)
        }

        dialogUtils = DialogUtils {
            Log.e("ceshi","弹窗返回$it")
            when(it){
                "定位到聊天位置" -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val localUrl = urlMapper.getNetworkUrl(StringObjectUtils.extractImageId(mVideoUri))
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
                            val intent = Intent(this@VideoPlayerActivity, MainActivity::class.java)
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
                        val localUrl = urlMapper.getNetworkUrl(StringObjectUtils.extractImageId(mVideoUri))
                        Log.e("ceshi","网络URL:$localUrl")
                        if (localUrl != null){
                            ImageToGalleryUtil.saveToGalleryAction(
                                localUrl,
                                this@VideoPlayerActivity,
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

        // 获取视频URI并播放
        val videoUri = intent.getParcelableExtra<android.net.Uri>("VIDEO_URI")
        videoUri?.let { uri ->
            binding.videoView.setVideoURI(uri)
            mVideoUri = uri.toString()
            // 准备完成后开始播放
            binding.videoView.setOnPreparedListener { mediaPlayer ->
                binding.progressBar.visibility = View.GONE
                binding.seekBar.max = mediaPlayer.duration
                mediaPlayer.start()
                startProgressUpdate()
            }

            // 视频播放完成
            binding.videoView.setOnCompletionListener {
                finish()
            }

            // 点击视频切换播放/暂停状态
            binding.videoView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    togglePlayPause()
                    true
                } else {
                    false
                }
            }

            binding.stop.setOnClickListener {
                togglePlayPause()
            }

            // 进度条拖动控制
            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        binding.videoView.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            // 准备视频
            binding.videoView.start()
        }
    }

    // 切换播放/暂停状态
    private fun togglePlayPause() {
        if (binding.videoView.isPlaying) {
            binding.videoView.pause()
            stopProgressUpdate()
        } else {
            binding.videoView.start()
            startProgressUpdate()
        }
    }

    // 开始更新进度条
    private fun startProgressUpdate() {
        progressUpdateRunnable = object : Runnable {
            override fun run() {
                val currentPos = binding.videoView.currentPosition
                binding.seekBar.progress = currentPos
                handler.postDelayed(this, 1000) // 每秒更新一次
            }
        }
        progressUpdateRunnable?.let {
            handler.post(it)
        }
    }

    // 停止更新进度条
    private fun stopProgressUpdate() {
        progressUpdateRunnable?.let {
            handler.removeCallbacks(it)
        }
    }

    override fun onPause() {
        super.onPause()
        if (binding.videoView.isPlaying) {
            binding.videoView.pause()
            stopProgressUpdate()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!binding.videoView.isPlaying && binding.videoView.currentPosition > 0) {
            binding.videoView.start()
            startProgressUpdate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProgressUpdate()
        binding.videoView.stopPlayback()
        handler.removeCallbacksAndMessages(null)
    }
}
