package com.newAi302.app.utils

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat.requestPermissions
import com.newAi302.app.MainActivity
import com.newAi302.app.R
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.MarkwonHtmlRenderer
import io.noties.markwon.html.TagHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import javax.sql.DataSource

//import io.noties.markwon.visitor.MarkwonVisitor

/**
 * 适配 Markwon 新 TagHandler 接口的 <img> 标签处理器
 * 支持解析 <img src="content://xxx" width="150" height="150"> 格式
 */
class CustomImgTagHandler(
    private val context: Context,
    private val listener: OnHtmlImageClickListener,
    private val textView: TextView,
    private val message: String
) : TagHandler() {

    // 新增：构造函数日志，确认是否被实例化
    init {
        Log.e("ceshi", "CustomImgTagHandler 被实例化了")
    }

    // Glide 实例（复用，避免重复创建）
    private val glide: RequestManager by lazy { Glide.with(context) }

    /**
     * 声明支持的标签：仅处理 "img" 标签
     */
    override fun supportedTags(): MutableCollection<String> {
        return mutableListOf("img") // 返回支持的标签名列表
    }

    /**
     * 核心处理逻辑：解析 <img> 标签并生成可点击图片
     */
    override fun handle(
        visitor: MarkwonVisitor,
        renderer: MarkwonHtmlRenderer,
        tag: HtmlTag
    ) {
        // 1. 从 HtmlTag 中获取 <img> 标签的属性（src/width/height）
        val attributes = tag.attributes() // 标签所有属性
        Log.e("ceshi", "当前标签属性: $attributes") // 打印所有属性，确认结构
        val imageSrc = attributes["src"] ?: return // 图片路径（content://xxx）
        val imageWidth = attributes["width"]?.toIntOrNull() ?: 150.dpToPx() // 默认150dp
        val imageHeight = attributes["height"]?.toIntOrNull() ?: 150.dpToPx() // 默认150dp

        Log.e("ceshi","图片插件：$imageSrc")
        // 合并权限请求，避免重复请求
//        val permissions = mutableListOf<String>()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
//            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
//        } else {
//            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
//        }
//        requestPermissions(activity,permissions.toTypedArray(), 0)
//        val systemUri = convertMiuiPrivateUri(context, imageSrc)
//        if (systemUri != null) {
//            // 用系统标准 Uri 加载图片
//            //loadImageWithSystemUri(systemUri)
//        } else {
//            Log.e("ceshi", "无法转换私有路径: $imageSrc")
//        }
        // 1. 解码得到真实文件路径
        val filePath = decodeMiuiPrivateUri(imageSrc)
        if (filePath == null) {
            Log.e("ceshi", "解码失败，无法继续")
            return
        }
        Log.e("ceshi", "解码后的文件路径: $filePath")

        // 2. 通过文件路径查询系统Uri
        val systemUri = getSystemUriByFilePath(context, filePath)
        if (systemUri != null) {
            // 3. 用系统Uri加载图片（有权限访问）
            //loadImage(systemUri)
            Log.e("ceshi", "解码成功的URL:$systemUri")
        } else {
            Log.e("ceshi", "0解码失败，无法继续")
//            // 4. 转换失败，提示用户重新选择
//            showToast("无法访问该图片，请重新选择")
//            // 可选：自动打开系统选择器
//            (context as? Activity)?.let { activity ->
//                pickImageAgain(activity)
//            }
        }
        /*CoroutineScope(Dispatchers.Main).launch {
            // 2. 创建可点击的 ImageView（用于显示图片）
            val imageView = ImageView(context).apply {
                layoutParams = ViewGroup.LayoutParams(imageWidth, imageHeight)
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = "HTML 图片"
                isClickable = true // 启用点击
                isFocusable = true // 确保焦点（部分场景需）

                // 3. 绑定点击事件（传递 src 路径）
                setOnClickListener {
                    Log.e("ceshi", "图片被点击，路径: $imageSrc") // 增加点击日志
                    listener.onImageClick(imageSrc)
                }

                // 4. 加载图片（支持 content:///http:///file:// 协议）
                glide.load(systemUri)
                    .placeholder(android.R.drawable.ic_menu_gallery) // 加载中占位
                    .error(android.R.drawable.stat_notify_error) // 加载失败占位
                    .into(this)

            }

            // 5. 将 ImageView 转为 Drawable（Markwon 需 Drawable 渲染）
            val viewDrawable = ViewDrawable(imageView)
            viewDrawable.setBounds(0, 0, imageWidth, imageHeight)

            // 6. 将 Drawable 插入到 Markwon 渲染流程（关键步骤）
            visitor.builder().setSpan(
                viewDrawable,
                tag.start(), // 标签在文本中的起始索引
                tag.end(),   // 标签在文本中的结束索引
                100 // 图片优先级
            )
        }



        // 7. 清除原标签文本（避免保留 "<img...>" 原始字符串）
        val spannableBuilder = visitor.builder() as? SpannableStringBuilder
        spannableBuilder?.replace(tag.start(), tag.end(), "")*/
        if (systemUri != null) {
            // 改用 CustomTarget 监听加载完成
            /*glide.asDrawable()
                .load(systemUri)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(object : CustomTarget<Drawable>(imageWidth, imageHeight) {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        // 1. 创建 ImageView 并设置加载完成的图片
                        val imageView = ImageView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(imageWidth, imageHeight)
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            setImageDrawable(resource) // 直接设置加载完成的图片
                            isClickable = true
                            setOnClickListener {
                                listener.onImageClick(imageSrc)
                            }
                        }

                        // 2. 转为 ViewDrawable
                        val viewDrawable = ViewDrawable(imageView)
                        viewDrawable.setBounds(0, 0, imageWidth, imageHeight)

                        // 3. 插入 Markwon 渲染流程
                        visitor.builder().setSpan(
                            viewDrawable,
                            tag.start(),
                            tag.end(),
                            100
                        )

                        // 4. 清除原标签文本
                        val spannableBuilder = visitor.builder() as? SpannableStringBuilder
                        spannableBuilder?.replace(tag.start(), tag.end(), "")

                        Log.e("ceshi", "新 Spannable 内容: ${spannableBuilder.toString()}")
                        // 5. 通知 Markwon 刷新（关键！）
                        // 关键：手动更新 TextView 的文本（触发重新渲染）
                        textView.post {
                            textView.text = message // 重新设置 Spannable
                            textView.invalidate()
                            textView.requestLayout()
                        }

                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // 加载被取消时，设置占位符
                        val imageView = ImageView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(imageWidth, imageHeight)
                            setImageDrawable(placeholder)
                        }
                        val viewDrawable = ViewDrawable(imageView)
                        viewDrawable.setBounds(0, 0, imageWidth, imageHeight)
                        visitor.builder().setSpan(
                            viewDrawable,
                            tag.start(),
                            tag.end(),
                            100
                        )
                    }
                })*/
            glide.asBitmap()
                .load(systemUri)
                .into(object : CustomTarget<Bitmap>(imageWidth, imageHeight) {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val bitmapDrawable = BitmapDrawable(context.resources, resource)
                        bitmapDrawable.setBounds(0, 0, imageWidth, imageHeight)

                        visitor.builder().setSpan(
                            bitmapDrawable,
                            tag.start(),
                            tag.end(),
                            1000
                        )

                        textView.post {
                            textView.invalidate()
                            textView.requestLayout()
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // 加载失败时的处理
                    }
                })
        }
    }

    /**
     * 辅助类：将 View 转为 Drawable（适配 Markwon 渲染需求）
     */
    private class ViewDrawable(private val view: View) : Drawable() {
        init {
            // 测量 View 尺寸（避免宽高为 0）
            val widthSpec = View.MeasureSpec.makeMeasureSpec(
                view.layoutParams.width,
                View.MeasureSpec.EXACTLY
            )
            val heightSpec = View.MeasureSpec.makeMeasureSpec(
                view.layoutParams.height,
                View.MeasureSpec.EXACTLY
            )
            view.measure(widthSpec, heightSpec)
            Log.e("ceshi", "View 尺寸: ${view.measuredWidth} x ${view.measuredHeight}")
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        }

        // 绘制 View 到 Canvas
        override fun draw(canvas: Canvas) {
            view.draw(canvas)
        }

        // 透明度设置
        override fun setAlpha(alpha: Int) {
            view.alpha = alpha.toFloat()
        }

        // 颜色过滤
        override fun setColorFilter(colorFilter: ColorFilter?) {
            view.background?.colorFilter = colorFilter
        }

        // 透明度模式（半透明，适配 View 背景）
        override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
    }

    /**
     * DP 转 PX（适配不同屏幕）
     */
    private fun Int.dpToPx(): Int =
        (this * context.resources.displayMetrics.density).toInt()

    /**
     * 图片点击事件回调接口
     */
    interface OnHtmlImageClickListener {
        fun onImageClick(imagePath: String) // imagePath 为 <img src="xxx"> 中的 src 值
    }

    // 在 CustomImgTagHandler 中用系统原生方式加载
    private fun loadImageWithSystemResolver(uri: Uri) {
        try {
            // 用 ContentResolver 直接读流（模拟默认逻辑）
            val inputStream = context.contentResolver.openInputStream(uri)
            val drawable = Drawable.createFromStream(inputStream, uri.toString())
            // 将 drawable 插入 Markwon 渲染流程
            // ...
        } catch (e: SecurityException) {
            Log.e("ceshi", "系统方式仍无法访问：${e.message}")
        }
    }

    /**
     * 将小米私有路径转换为系统标准 Uri
     * @param context 上下文
     * @param privateUri 小米私有路径（如 content://com.miui.gallery.open/...）
     * @return 系统标准 Uri（content://media/external/images/...）或 null
     */
    fun convertMiuiPrivateUri(context: Context, privateUri: String): Uri? {
        // 1. 从私有 Uri 中提取文件名（需解析路径，示例假设路径格式固定）
        val fileName = privateUri.substringAfterLast("/") // 如 "IMG_20250725_153926.jpg"

        // 2. 查询 MediaStore，根据文件名找标准 Uri
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
        )
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            }
        }
        return null
    }

    /**
     * 通过真实文件路径查询MediaStore，获取系统标准Uri
     * @param context 上下文
     * @param filePath 真实文件路径（如/storage/emulated/0/DCIM/Camera/xxx.jpg）
     * @return 系统标准Uri（content://media/external/images/...）或null
     */
    fun getSystemUriByFilePath(context: Context, filePath: String): Uri? {
        val file = File(filePath)
        // 1. 先检查文件是否存在（不存在直接返回）
        if (!file.exists()) {
            Log.e("ceshi", "文件不存在: $filePath")
            return null
        }

        // 2. 查询MediaStore，通过文件路径匹配
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA // 存储文件绝对路径的字段
        )
        // 条件：路径完全匹配
        val selection = "${MediaStore.Images.Media.DATA} = ?"
        val selectionArgs = arrayOf(filePath)

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                val systemUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                Log.e("ceshi", "成功转换为系统Uri: $systemUri")
                return systemUri
            }
        }
        Log.e("ceshi", "MediaStore中未找到该文件: $filePath")
        return null
    }

    /**
     * 解码小米私有路径，提取真实文件路径
     * @param privateUri 小米私有路径（content://com.miui.gallery.open/raw/...）
     * @return 真实文件路径（如/storage/emulated/0/DCIM/Camera/xxx.jpg）或null
     */
    fun decodeMiuiPrivateUri(privateUri: String): String? {
        // 1. 校验是否是小米私有路径
        if (!privateUri.startsWith("content://com.miui.gallery.open/raw/")) {
            Log.e("ceshi", "不是小米私有路径: $privateUri")
            return null
        }
        // 2. 提取编码后的路径部分（如%2Fstorage%2Femulated%2F0%2F...）
        val encodedPath = privateUri.substringAfter("content://com.miui.gallery.open/raw/")
        // 3. URL解码得到真实文件路径
        return try {
            URLDecoder.decode(encodedPath, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            Log.e("ceshi", "路径解码失败: ${e.message}")
            null
        }
    }

}