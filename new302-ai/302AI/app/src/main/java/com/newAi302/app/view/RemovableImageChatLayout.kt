package com.newAi302.app.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.newAi302.app.R
import com.newAi302.app.data.ImageBack
import com.newAi302.app.infa.OnWordPrintOverClickListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * author :
 * e-mail :
 * time   : 2025/7/9
 * desc   :
 * version: 1.0
 */
@SuppressLint("ClickableViewAccessibility")
class RemovableImageChatLayout(context: Context, attrs: AttributeSet? = null, private val listenerOver: OnWordPrintOverClickListener) :
    LinearLayout(context, attrs) {
        private var imageId = 0
    private var resUrl = ""
    private var isLongPressed = false  // 标记是否已触发长按

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.layout_removable_chat_image, this, true)

        // 绑定删除按钮事件
        findViewById<ImageView>(R.id.imageViewDelete).setOnClickListener {
            (parent as? ViewGroup)?.removeView(this)
            listenerOver.onDeleteImagePosition(this.imageId)
        }

        //点击预览
        findViewById<ImageView>(R.id.imageViewMain).setOnClickListener {
            //(parent as? ViewGroup)?.removeView(this)
            //listenerOver.onPreImageClick(this.resUrl)
            false
        }
        findViewById<ImageView>(R.id.imageViewMain).setOnLongClickListener {
            isLongPressed = true
            listenerOver.onImageBackClick(ImageBack("imageLongClick",this.resUrl,0))
            false
        }
        findViewById<ImageView>(R.id.imageViewMain).setOnTouchListener(View.OnTouchListener { v, event ->
            Log.e("ceshi", "WebView按下事件0")
            // 处理触摸事件，例如记录点击位置等
            //false // 返回false表示事件没有被完全消费，可以继续传递到WebView内部处理（如点击链接）
            // 只处理按下（ACTION_DOWN）或抬起（ACTION_UP）事件（根据需求选择）
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 按下时记录日志（仅触发1次）
                    Log.e("ceshi", "WebView按下事件")
                    isLongPressed = false
                    false  // 让事件继续传递给WebView内部处理
                }

                MotionEvent.ACTION_UP -> {
                    // 抬起时记录日志（仅触发1次）
                    Log.e("ceshi", "WebView抬起事件")
                    if (!isLongPressed){
                        listenerOver.onImageBackClick(ImageBack("imageShortClick",this.resUrl,0))
                    }
                    false  // 让事件继续传递给WebView内部处理
                }

                else -> {
                    // 其他事件（如ACTION_MOVE）不处理
                    false
                }
            }
        })
    }

    // 设置主图片
    fun setImageResource(resUrl: String,imageId:Int,isFile:Boolean,fileName:String,fileSize:String) {
        //findViewById<ImageView>(R.id.imageViewMain).setImageResource(resId)
        this.imageId = imageId
        this.resUrl = resUrl
        CoroutineScope(Dispatchers.Main).launch {
            if (isFile){
                findViewById<LinearLayout>(R.id.fileLine).visibility = View.VISIBLE
                //findViewById<LinearLayout>(R.id.imageShowChatLine).visibility = View.GONE
                findViewById<TextView>(R.id.fileNameTv).text = fileName
                findViewById<TextView>(R.id.fileSizeTv).text = fileSize
                var image = 0
                if (fileName.contains("pdf")){
                    image = R.drawable.icon_file_new
                }else if (fileName.contains("word") || fileName.contains("doc")){
                    image = R.drawable.icon_file_word
                }else if (fileName.contains("ppt")){
                    image = R.drawable.icon_file_ppt
                }else if (fileName.contains("txt")){
                    image = R.drawable.icon_file_txt
                }else if (fileName.contains("xls")){
                    image = R.drawable.icon_file_excel
                }else{
                    image = R.drawable.icon_file_common
                }
                Glide.with(this@RemovableImageChatLayout)
                    .load(image)
                    .transform(
                        CenterCrop(),
                        RoundedCornersTransformation(4, 4, RoundedCornersTransformation.CornerType.ALL) // 第二个参数：圆角dp，第三个参数：边缘模糊度（0为无模糊）
                    )
                    .placeholder(android.R.drawable.ic_menu_gallery) // 加载中占位图
                    .error(android.R.drawable.stat_notify_error) // 加载失败占位图
                    .into(findViewById<ImageView>(R.id.imageViewMain))
            }else{
                findViewById<LinearLayout>(R.id.fileLine).visibility = View.GONE
                //findViewById<LinearLayout>(R.id.imageShowChatLine).visibility = View.VISIBLE
                Glide.with(this@RemovableImageChatLayout)
                    .load(resUrl)
                    .transform(
                        CenterCrop(),
                        RoundedCornersTransformation(4, 4, RoundedCornersTransformation.CornerType.ALL) // 第二个参数：圆角dp，第三个参数：边缘模糊度（0为无模糊）
                    )
                    .placeholder(android.R.drawable.ic_menu_gallery) // 加载中占位图
                    .error(android.R.drawable.stat_notify_error) // 加载失败占位图
                    .into(findViewById<ImageView>(R.id.imageViewMain))
            }
        }


    }
}