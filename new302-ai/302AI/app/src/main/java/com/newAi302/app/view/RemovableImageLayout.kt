package com.newAi302.app.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.newAi302.app.R
import com.newAi302.app.infa.OnWordPrintOverClickListener
import com.bumptech.glide.Glide

/**
 * author :
 * e-mail :
 * time   : 2025/7/9
 * desc   :
 * version: 1.0
 */
class RemovableImageLayout(context: Context, attrs: AttributeSet? = null,private val listenerOver: OnWordPrintOverClickListener) :
    LinearLayout(context, attrs) {
        private var imageId = 0
    private var resUrl = ""

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.layout_removable_image, this, true)

        // 绑定删除按钮事件
        findViewById<ImageView>(R.id.imageViewDelete).setOnClickListener {
            (parent as? ViewGroup)?.removeView(this)
            listenerOver.onDeleteImagePosition(this.imageId)
        }

        findViewById<ImageView>(R.id.imageViewDelete1).setOnClickListener {
            (parent as? ViewGroup)?.removeView(this)
            listenerOver.onDeleteImagePosition(this.imageId)
        }

        //点击预览
        findViewById<ImageView>(R.id.imageViewMain).setOnClickListener {
            (parent as? ViewGroup)?.removeView(this)
            listenerOver.onPreImageClick(this.resUrl)
        }
    }

    // 设置主图片
    fun setImageResource(resUrl: String,imageId:Int,isFile:Boolean,fileName:String,fileSize:String) {
        //findViewById<ImageView>(R.id.imageViewMain).setImageResource(resId)
        this.imageId = imageId
        this.resUrl = resUrl
        if (isFile){
            findViewById<ConstraintLayout>(R.id.fileLine).visibility = View.VISIBLE
            findViewById<TextView>(R.id.fileNameTv).text = fileName
            findViewById<TextView>(R.id.fileSizeTv).text = fileSize
            findViewById<ImageView>(R.id.imageViewDelete).visibility = View.GONE
            Glide.with(this)
                .load(R.drawable.icon_file)
                .placeholder(android.R.drawable.ic_menu_gallery) // 加载中占位图
                .error(android.R.drawable.stat_notify_error) // 加载失败占位图
                .into(findViewById<ImageView>(R.id.imageViewMain))
        }else{
            findViewById<ConstraintLayout>(R.id.fileLine).visibility = View.GONE
            findViewById<ImageView>(R.id.imageViewDelete).visibility = View.VISIBLE
            Glide.with(this)
                .load(resUrl)
                .placeholder(android.R.drawable.ic_menu_gallery) // 加载中占位图
                .error(android.R.drawable.stat_notify_error) // 加载失败占位图
                .into(findViewById<ImageView>(R.id.imageViewMain))
        }

    }
}