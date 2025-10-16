package com.newAi302.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.newAi302.app.R
import com.newAi302.app.data.ChatBackMessage
import com.newAi302.app.data.MediaItem
import com.newAi302.app.utils.DialogUtils
import com.newAi302.app.utils.ViewAnimationUtils
import com.bumptech.glide.Glide

class MediaAdapter(
    private val context: Context,
    private val mediaItems: List<MediaItem>,
    private val onItemClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {
    private var longTouch = false
    private lateinit var dialogUtils: DialogUtils
    private var nowPosition = 0

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
        val playIcon: ImageView = itemView.findViewById(R.id.play_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        var mMediaItem = mediaItems[position]

        // 使用Glide加载缩略图
        Glide.with(holder.itemView.context)
            .load(mediaItem.uri)
            .thumbnail(0.1f)
            .into(holder.thumbnail)

        // 视频显示播放图标
        holder.playIcon.visibility = if (mediaItem.isVideo) View.VISIBLE else View.GONE

        // 点击事件
        /*holder.itemView.setOnClickListener {
            if (!longTouch){
                onItemClick(mediaItem)
            }else{
                longTouch = false
            }
        }*/
        holder.itemView.setOnTouchListener(View.OnTouchListener { v, event ->
            Log.e("ceshi", "WebView按下事件0")
            // 处理触摸事件，例如记录点击位置等
            false // 返回false表示事件没有被完全消费，可以继续传递到WebView内部处理（如点击链接）
            // 只处理按下（ACTION_DOWN）或抬起（ACTION_UP）事件（根据需求选择）
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 按下时记录日志（仅触发1次）
                    Log.e("ceshi", "WebView按下事件")
                    longTouch = false
                    false  // 让事件继续传递给WebView内部处理
                }

                MotionEvent.ACTION_UP -> {
                    // 抬起时记录日志（仅触发1次）
                    Log.e("ceshi", "WebView抬起事件")
                    if (!longTouch){
//                        // 保存上一次点击的位置
//                        val previousClickedPosition = lastSelectedPosition
//                        // 更新当前点击的位置
//                        lastSelectedPosition = position
//
//                        // 通知 RecyclerView 更新之前点击的 item 和当前点击的 item
//                        if (previousClickedPosition != -1 && previousClickedPosition<chatList.size-1) {
//                            notifyItemChanged(previousClickedPosition)
//                        }
//                        notifyItemChanged(position)
                        mMediaItem.type = "查看"
                        onItemClick(mMediaItem)
                    }
                    false  // 让事件继续传递给WebView内部处理
                }

                else -> {
                    // 其他事件（如ACTION_MOVE）不处理
                    false
                }
            }
        })

        holder.itemView.setOnLongClickListener {
            Log.e("ceshi","长按图片")
            longTouch = true
            nowPosition = position
            ViewAnimationUtils.performClickEffect(it)
            val options = mutableListOf("定位到聊天位置","复制","分享","保存到相册","上传到档案库","添加到知识库")
            dialogUtils.setupPopupWindow(options,"ResourceActivity",context)
            dialogUtils.showPopup(it)
            false
        }

        dialogUtils = DialogUtils {
            Log.e("ceshi","弹窗返回$it")
            when(it){
                "定位到聊天位置" -> {
                    mMediaItem = mediaItems[nowPosition]
                    mMediaItem.type = "定位到聊天位置"
                    onItemClick(mMediaItem)
                }
                "复制" -> {
                    mMediaItem = mediaItems[nowPosition]
                    mMediaItem.type = "复制"
                    onItemClick(mMediaItem)
                }
                "分享" -> {
                    mMediaItem = mediaItems[nowPosition]
                    mMediaItem.type = "分享"
                    onItemClick(mMediaItem)
                }
                "保存到相册" -> {
                    mMediaItem = mediaItems[nowPosition]
                    mMediaItem.type = "保存到相册"
                    onItemClick(mMediaItem)
                }
                "上传到档案库" -> {
                    mMediaItem = mediaItems[nowPosition]
                    mMediaItem.type = "上传到档案库"
                    onItemClick(mMediaItem)
                }
                "添加到知识库" -> {
                    mMediaItem = mediaItems[nowPosition]
                    mMediaItem.type = "添加到知识库"
                    onItemClick(mMediaItem)
                }


            }
        }

    }

    override fun getItemCount(): Int = mediaItems.size
}
