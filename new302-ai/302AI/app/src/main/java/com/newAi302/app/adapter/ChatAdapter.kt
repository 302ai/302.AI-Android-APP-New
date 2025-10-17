package com.newAi302.app.adapter

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/3/21
 * desc   :
 * version: 1.0
 */
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.style.URLSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.newAi302.app.MainActivity
import com.newAi302.app.MyApplication.Companion.myApplicationContext
import com.newAi302.app.R
import com.newAi302.app.data.BackChatToolItem
import com.newAi302.app.data.ChatBackMessage
import com.newAi302.app.data.ChatMessage
import com.newAi302.app.data.ImageBack
import com.newAi302.app.datastore.ImageUrlMapper
import com.newAi302.app.http.ImageDownloadService
import com.newAi302.app.http.ImageDownloader
import com.newAi302.app.http.ResourceUtils.saveResource
import com.newAi302.app.infa.OnItemClickListener
import com.newAi302.app.infa.OnMarkdownImageClickListener
import com.newAi302.app.infa.OnWordPrintOverClickListener
import com.newAi302.app.room.ChatItemChat
import com.newAi302.app.utils.CustomImgTagHandler
import com.newAi302.app.utils.CustomToast
import com.newAi302.app.utils.CustomUrlSpan
import com.newAi302.app.utils.DialogUtils
import com.newAi302.app.utils.ImageClickPlugin
import com.newAi302.app.utils.StringObjectUtils
import com.newAi302.app.utils.StringObjectUtils.generateRandomFilename
import com.newAi302.app.utils.SystemUtils
import com.newAi302.app.utils.ViewAnimationUtils
import com.newAi302.app.view.RemovableImageChatLayout
import com.newAi302.app.view.RemovableImageLayout

import com.newAi302.app.view.ThreeCircularLoadingAnim
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.notifyAll
import retrofit2.Retrofit
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

class ChatAdapter(private var messageList: List<ChatMessage>, private val context: Context ,private val listener: OnItemClickListener) :
    OnWordPrintOverClickListener,
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    // 用于标记每条消息是否已经加载过
    private val loadedMessages = mutableSetOf<String>()
    private var counter = 0
    // 状态存储集合（使用LinkedHashMap保持顺序）
    private var isStop = false
    private var isChatNew = false
    private var pictureUrl = ""
    private var pictureNumber = 0
    private var isPicture = false

    private var picturePosition = mutableSetOf<Int>()
    private var nowPosition = 0
    private var deepMessage = ""
    private var buttonStop = false
    private var chatType = 0
    private var deepThinkingTime = 0

    private val handler = Handler(Looper.getMainLooper())
    private val handlerDeep = Handler(Looper.getMainLooper())
    private var currentLength = 0 // 当前已显示的字符数
    private var currentLengthDeep = 0 // 当前已显示的字符数
    private var fullSpannable: Spannable? = null // 完整解析后的 Markdown 内容（带样式）
    private var fullSpannableDeep: Spannable? = null // 完整解析后的 Markdown 内容（带样式）

    private var scaleAnimator: AnimatorSet? = null
    private var scaleAnimation: ValueAnimator? = null

    private var chatItemOnClick = false

    private var clearNumber = 0

    private var clearNumbers = mutableSetOf<Int>()
    private var preSetWordNumbers = mutableSetOf<Int>()

    private var modelType = ""
    private var modelTypePosition = 0

    private var hashMapModelType = HashMap<Int, String>()

    private var isClearText = true

    private var isOpenPreEye = false
    var currentMessage: String? = null
    private var deepLineOnClick = false

    private lateinit var dialogUtils: DialogUtils

    private var isLongPressed = false  // 标记是否已触发长按

    private var copyPosition = 0
    private var threePosition =0

    private var userPosition = 0

    private var curPosition = 0
    private var isNewChat = false
    private var imageUrlLocalList = CopyOnWriteArrayList<String>()
    private var fileName = ""
    private var fileSize = ""

    private var isGood = false
    private lateinit var mHolder: ChatViewHolder
    private var badPosition = 0

    private var isImageChatLine = false

    private lateinit var imageBack: ImageBack

    // 持有 RecyclerView的引用（需在初始化时传入）
    private var recyclerView: RecyclerView? = null

    private var hashMapUrlHolder = HashMap<String, ChatViewHolder>()

    private var goneDeepTextNumbers = HashSet<Int>()
    private var hashMapGoneDeepTextHolder = HashMap<Int, ChatViewHolder>()

    private var codeNameStr = "code.html"


    // 定义消息类型常量
    private companion object {
        const val TYPE_USER = 0
        const val TYPE_ROBOT = 1
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deepTextView: TextView = itemView.findViewById(R.id.text1)
        val deepLine:LinearLayout = itemView.findViewById(R.id.deepLine)
        val downDeepImage: ImageView = itemView.findViewById(R.id.downDeepImage)
        val deepThinkTime: TextView = itemView.findViewById(R.id.deepThinkTime)
        val chatToolLine:ConstraintLayout = itemView.findViewById(R.id.chatToolLine)
        val copyLine:LinearLayout = itemView.findViewById(R.id.copyLine)
        val againLine:LinearLayout = itemView.findViewById(R.id.againLine)
        val goodLine:LinearLayout = itemView.findViewById(R.id.goodLine)
        val badLine:LinearLayout = itemView.findViewById(R.id.badLine)
        val chatItemLayout:ConstraintLayout = itemView.findViewById(R.id.chat_item_layout)
        val loadingThreeLine:LinearLayout = itemView.findViewById(R.id.loadingThreeLine)
        val loadThreeImage: ThreeCircularLoadingAnim = itemView.findViewById<ThreeCircularLoadingAnim>(R.id.loadThreeImage)
        val msgLine:LinearLayout = itemView.findViewById(R.id.msgLine)
        val messageText:TextView = itemView.findViewById<TextView>(R.id.messageTv)

        val messageMeTv:TextView = itemView.findViewById<TextView>(R.id.messageTv)

        val moreLine:LinearLayout = itemView.findViewById(R.id.moreLine)

        val badImage: ImageView = itemView.findViewById(R.id.badImage)
        val goodImage: ImageView = itemView.findViewById(R.id.goodImage)

        val codePreConst:ConstraintLayout = itemView.findViewById(R.id.codePreConst)

        val fileChatLine:LinearLayout = itemView.findViewById(R.id.fileChatLine)
        val imageChatLine:LinearLayout = itemView.findViewById(R.id.imageChatLine)
        val fileHorScr: HorizontalScrollView = itemView.findViewById(R.id.fileHorScr)
        val imageHorScr: HorizontalScrollView = itemView.findViewById(R.id.imageHorScr)

        val codeFileNameTv:TextView = itemView.findViewById(R.id.codeFileNameTv)



    }

    override fun getItemViewType(position: Int): Int {
        val chatMessage = messageList[position]
        return if (chatMessage.isMe) {
            TYPE_USER
        } else {
            TYPE_ROBOT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layoutRes = when (viewType) {
            TYPE_USER -> R.layout.me_list_item
            TYPE_ROBOT -> R.layout.robot_list_item // 可替换为右侧对齐的布局
            else -> throw IllegalArgumentException("Invalid view type")
        }
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutRes, parent, false)
        return ChatViewHolder(view)
    }

    @SuppressLint("ResourceAsColor", "ClickableViewAccessibility", "SetTextI18n")
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatMessage = messageList[position]
        curPosition = position
        //holder.messageText.text = chatMessage.message
        if (chatMessage.message.contains("file:///android_asset/loading.html")){
            holder.loadingThreeLine.visibility = View.VISIBLE
            holder.messageText.visibility = View.GONE
            holder.deepLine.visibility = View.GONE
            holder.loadThreeImage.startAnim()
        }else{
            holder.loadThreeImage.stopAnim()
            holder.loadingThreeLine.visibility = View.GONE
            holder.messageText.visibility = View.VISIBLE
            scaleAnimation?.cancel() // 销毁时停止动画
        }

        if (chatMessage.message=="这是删除过的内容变为空白"){
            holder.chatItemLayout.visibility = View.GONE
            holder.chatItemLayout.layoutParams = holder.chatItemLayout.layoutParams.apply {
                height = 0 // 关键：设置高度为0
                // 3. 清除margin（如果有）
                if (this is ViewGroup.MarginLayoutParams) {
                    topMargin = 0
                    bottomMargin = 0
                }
            }

            // 4. 强制刷新布局
            holder.chatItemLayout.requestLayout()

        }else{
            holder.chatItemLayout.visibility = View.VISIBLE
            holder.chatItemLayout.layoutParams = holder.chatItemLayout.layoutParams.apply {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        if (chatMessage.message.contains("```")){
            holder.codePreConst.visibility = View.VISIBLE
            val str = StringObjectUtils.detectProgrammingLanguage(chatMessage.message)
            var fileLast = ".html"
            val preCode = messageList[position-1].message
            if(preCode.contains("java") || preCode.contains("Java")){
                fileLast = ".java"
            }else if (preCode.contains("html") || preCode.contains("Html")){
                fileLast = ".html"
            }else if (preCode.contains("python") || preCode.contains("Python")){
                fileLast = ".python"
            }
            Log.e("ceshi","返回文件扩展名字$str")
            codeNameStr = "code"+generateRandomFilename(6,6)+fileLast
            holder.codeFileNameTv.text = codeNameStr
        }else{
            holder.codePreConst.visibility = View.GONE
        }

        holder.imageChatLine.removeAllViews()
        holder.fileChatLine.removeAllViews()

        if (chatMessage.message.contains(".jpg") && chatMessage.isMe){
            holder.imageHorScr.visibility = View.VISIBLE
        }
        if (chatMessage.message.contains("media.documents/") && chatMessage.isMe){
            holder.fileHorScr.visibility = View.VISIBLE
        }
        if (chatMessage.message.contains(".jpg") || chatMessage.message.contains("media.documents/")){
            val urlLists = StringObjectUtils.extractAllImageUrls(chatMessage.message)
            Log.e("ceshi","图片的数量${urlLists}")
            for (url in urlLists){
                addNewImageView(url,holder, context)
            }
        }


        holder.moreLine.setOnClickListener {
            ViewAnimationUtils.performClickEffect(holder.moreLine)
            //val options = mutableListOf("分享","截图","选择文本","上传到档案库","添加到知识库")
            val options = mutableListOf(ContextCompat.getString(context, R.string.screenshot_toast_message),
                ContextCompat.getString(context, R.string.select_text_toast_message))
            threePosition = position
            dialogUtils.setupPopupWindow(options,"moreLine",context)
            dialogUtils.showPopup(it)
        }
        var chatToolItem = ChatBackMessage(chatMessage.message,chatMessage.isMe,"",position,codeNameStr)
        holder.copyLine.setOnClickListener {
            val fullText = messageList[position].message.trimIndent()
            val mFullText = StringObjectUtils.convertLatexFormat(fullText)
            if (messageList[position].message.contains("&&&&&&")){
                SystemUtils.copyTextToClipboard(context,AfterAmpersand(mFullText))
            }else{
                SystemUtils.copyTextToClipboard(context,messageList[position].message)
            }

            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(holder.copyLine)
            //Toast.makeText(context, "复制", Toast.LENGTH_SHORT).show()
            chatToolItem.doType = "robotCopy"
            listener.onBackFunctionClick(chatToolItem)
            // 使用自定义 Toast
            CustomToast.makeText(
                context = context,
                message = ContextCompat.getString(context, R.string.copy_toast_message),
                duration = Toast.LENGTH_SHORT,
                gravity = Gravity.CENTER
            ).show()
        }


        holder.goodLine.setOnClickListener {
            SystemUtils.copyTextToClipboard(context,messageList[position].message)
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            //Toast.makeText(context, "复制", Toast.LENGTH_SHORT).show()
            if (!isGood){
                isGood = true
                holder.goodImage.setImageResource(R.drawable.icon_good_color)
                chatToolItem.doType = "good"
                listener.onBackFunctionClick(chatToolItem)
                // 使用自定义 Toast
                CustomToast.makeText(
                    context = context,
                    message = ContextCompat.getString(context, R.string.good_toast_message),
                    duration = Toast.LENGTH_SHORT,
                    gravity = Gravity.CENTER
                ).show()
            }else{
                isGood = false
                holder.goodImage.setImageResource(R.drawable.icon_tool_good)
                chatToolItem.doType = "cancelGood"
                listener.onBackFunctionClick(chatToolItem)
                // 使用自定义 Toast
                CustomToast.makeText(
                    context = context,
                    message = ContextCompat.getString(context, R.string.cancel_good_toast_message),
                    duration = Toast.LENGTH_SHORT,
                    gravity = Gravity.CENTER
                ).show()
            }

        }
        Log.e("ceshi","好坏${chatMessage.isBad},,${chatMessage.isGood}")
        if (chatMessage.isGood){
            holder.goodImage.setImageResource(R.drawable.icon_good_color)
        }else{
            holder.goodImage.setImageResource(R.drawable.icon_tool_good)
        }
        if (chatMessage.isBad){
            holder.badImage.setImageResource(R.drawable.icon_bad_color)
        }else{
            holder.badImage.setImageResource(R.drawable.icon_bad)
        }

        holder.badLine.setOnClickListener {
            badPosition = position
            mHolder = holder
            SystemUtils.copyTextToClipboard(context,messageList[position].message)
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            //Toast.makeText(context, "复制", Toast.LENGTH_SHORT).show()
            holder.badImage.setImageResource(R.drawable.icon_bad_color)
            chatToolItem.doType = "bad"
            listener.onBackFunctionClick(chatToolItem)
            // 使用自定义 Toast
            CustomToast.makeText(
                context = context,
                message = ContextCompat.getString(context, R.string.bad_toast_message),
                duration = Toast.LENGTH_SHORT,
                gravity = Gravity.CENTER
            ).show()
        }

        holder.againLine.setOnClickListener {
            //SystemUtils.copyTextToClipboard(context,messageList[position].message)
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            //Toast.makeText(context, "复制", Toast.LENGTH_SHORT).show()
            chatToolItem.doType = "againRobot"
            listener.onBackFunctionClick(chatToolItem)
            // 使用自定义 Toast
            CustomToast.makeText(
                context = context,
                message = ContextCompat.getString(context, R.string.again_toast_message),
                duration = Toast.LENGTH_SHORT,
                gravity = Gravity.CENTER
            ).show()
        }

        holder.codePreConst.setOnClickListener {
            chatToolItem.doType = "codePre"
            listener.onBackFunctionClick(chatToolItem)
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            //Toast.makeText(context, "预览", Toast.LENGTH_SHORT).show()
            // 使用自定义 Toast
            CustomToast.makeText(
                context = context,
                message = ContextCompat.getString(context, R.string.pre_code_toast_message),
                duration = Toast.LENGTH_SHORT,
                gravity = Gravity.CENTER
            ).show()
        }

        dialogUtils = DialogUtils {
            Log.e("ceshi","弹窗返回$it")
            var chatItem = ChatBackMessage(chatMessage.message,chatMessage.isMe,"",position,codeNameStr)
            when(it){
                "分享" -> {
                    chatItem.doType = "share"
                    listener.onBackFunctionClick(chatItem)
                }
                "截图" -> {
                    CustomToast.makeText(
                        context = context,
                        message = ContextCompat.getString(context, R.string.screenshot_toast_message),
                        duration = Toast.LENGTH_SHORT,
                        gravity = Gravity.CENTER
                    ).show()
                    chatItem.doType = "screenshot"
                    listener.onBackFunctionClick(chatItem)
                }
                "选择文本" -> {
                    CustomToast.makeText(
                        context = context,
                        message = ContextCompat.getString(context, R.string.select_text_toast_message),
                        duration = Toast.LENGTH_SHORT,
                        gravity = Gravity.CENTER
                    ).show()
                    chatItem.doType = "chooseText"
                    chatItem.message = messageList[threePosition].message
                    listener.onBackFunctionClick(chatItem)
                }
                "上传到档案库" -> {
                    chatItem.doType = "uploadArchives"
                    listener.onBackFunctionClick(chatItem)
                }
                "添加到知识库" -> {
                    chatItem.doType = "uploadArchives"
                    listener.onBackFunctionClick(chatItem)
                }
                "用户复制" -> {
                    CustomToast.makeText(
                        context = context,
                        message = ContextCompat.getString(context, R.string.copy_toast_message),
                        duration = Toast.LENGTH_SHORT,
                        gravity = Gravity.CENTER
                    ).show()
                    chatItem.doType = "userCopy"
                    chatItem.message = messageList[copyPosition].message
                    listener.onBackFunctionClick(chatItem)
                }
                "用户编辑" -> {
                    CustomToast.makeText(
                        context = context,
                        message = ContextCompat.getString(context, R.string.user_edit_toast_message),
                        duration = Toast.LENGTH_SHORT,
                        gravity = Gravity.CENTER
                    ).show()
                    chatItem.doType = "userEdit"
                    chatItem.position = nowPosition
                    chatItem.message = messageList[nowPosition].message
                    listener.onBackFunctionClick(chatItem)
                }
                "用户重试" -> {
                    CustomToast.makeText(
                        context = context,
                        message = ContextCompat.getString(context, R.string.again_toast_message),
                        duration = Toast.LENGTH_SHORT,
                        gravity = Gravity.CENTER
                    ).show()
                    chatItem.doType = "userAgain"
                    chatItem.position = nowPosition
                    chatItem.message = messageList[nowPosition].message
                    listener.onBackFunctionClick(chatItem)
                }

                "用户图片复制" -> {
                    chatItem.doType = "userImageCopy"
                    chatItem.position = nowPosition
                    chatItem.message = imageBack.url
                    listener.onBackFunctionClick(chatItem)
                }
                "用户图片分享" -> {
//                    chatItem.doType = "userAgain"
//                    chatItem.position = nowPosition
//                    chatItem.message = messageList[nowPosition].message
//                    listener.onBackFunctionClick(chatItem)
                }
                "用户图片上传到档案库" -> {
//                    chatItem.doType = "userAgain"
//                    chatItem.position = nowPosition
//                    chatItem.message = messageList[nowPosition].message
//                    listener.onBackFunctionClick(chatItem)
                }
                "用户图片添加到知识库" -> {
//                    chatItem.doType = "userAgain"
//                    chatItem.position = nowPosition
//                    chatItem.message = messageList[nowPosition].message
//                    listener.onBackFunctionClick(chatItem)
                }


            }
        }

        if (chatMessage.isMe){
            holder.messageText.setOnLongClickListener {
                isLongPressed = true
                // 长按事件触发时执行的逻辑
                Log.e("ceshi","按钮被长按了")
                copyPosition = position
                userPosition = position
                ViewAnimationUtils.performClickEffect(it)
                nowPosition = position
                val options = mutableListOf(ContextCompat.getString(context, R.string.copy_toast_message),
                    ContextCompat.getString(context, R.string.edit_message),
                    ContextCompat.getString(context, R.string.again_toast_message))
                dialogUtils.setupPopupWindow(options,"meChatList",context)
                dialogUtils.showPopup(it)
                // 返回true表示消费了该事件，不会再触发点击事件
                // 返回false则会在长按结束后触发点击事件
                false
            }
        }


        Log.e("ceshi","要隐藏的：$goneDeepTextNumbers")
        for (gonePosition in goneDeepTextNumbers){
            if (gonePosition == position){
                Log.e("ceshi","0要隐藏的：$gonePosition")
                val holder2 = hashMapGoneDeepTextHolder.get(gonePosition)
                holder2?.deepTextView?.visibility = View.GONE
                holder2?.downDeepImage?.setImageResource(R.drawable.icon_down)
            }
        }

        holder.deepLine.setOnClickListener {
            if (!deepLineOnClick){
                holder.deepTextView.visibility = View.VISIBLE
                deepLineOnClick = true
                holder.downDeepImage.setImageResource(R.drawable.icon_up)
                goneDeepTextNumbers.remove(position)
                hashMapGoneDeepTextHolder.remove(position)
            }else{
                holder.deepTextView.visibility = View.GONE
                deepLineOnClick = false
                holder.downDeepImage.setImageResource(R.drawable.icon_down)
                goneDeepTextNumbers.add(position)
                hashMapGoneDeepTextHolder.put(position,holder)
            }

        }



        Log.e("ceshi","是否有深度返回${chatMessage.message.contains("&&&&&&")}")
        if (chatMessage.message.contains("&&&&&&")){
            holder.deepLine.visibility = View.VISIBLE
            holder.deepTextView.visibility = View.VISIBLE
            holder.downDeepImage.setImageResource(R.drawable.icon_up)
            deepLineOnClick = true
            Log.e("ceshi","1要隐藏的：$goneDeepTextNumbers")
            for (gonePosition in goneDeepTextNumbers){
                if (gonePosition == position){
                    Log.e("ceshi","2要隐藏的：$gonePosition")
                    val holder2 = hashMapGoneDeepTextHolder.get(gonePosition)
                    holder2?.deepTextView?.visibility = View.GONE
                    holder2?.downDeepImage?.setImageResource(R.drawable.icon_down)
                }
            }

        }else{
            holder.deepLine.visibility = View.GONE
            holder.deepTextView.visibility = View.GONE
        }

        if (chatMessage.message.contains(".jpg") || chatMessage.message.contains("media.documents/")){
            setMessageUi(holder,StringObjectUtils.extractInfoFromString(chatMessage.message))
        }else{
            setMessageUi(holder,chatMessage.message)
        }



    }

    override fun getItemCount(): Int {
        return messageList.size
    }




    override fun onViewRecycled(holder: ChatViewHolder) {
        super.onViewRecycled(holder)

    }

    fun upDatePosition(position: Int){
        this.nowPosition = position
    }

    fun upDateDeepMessage(deepMessage: String){
        this.deepMessage = deepMessage
        notifyDataSetChanged()
    }

    private fun setMessageUi(holder: ChatViewHolder,message:String){
        // 1. 图片点击监听器（用弱引用避免内存泄漏）
        val imageClickListener = object : CustomImgTagHandler.OnHtmlImageClickListener {
            override fun onImageClick(imagePath: String) {
                Log.e("HtmlImageClick", "点击图片，路径: $imagePath")
                //previewLocalImage(imagePath) // 预览本地图片（content:// 路径）
            }
        }



            // 初始化Markwon（包含常用插件）
        val markwon = Markwon.builder(context)
            .usePlugin(CorePlugin.create()) // 核心解析插件
            .usePlugin(MarkwonInlineParserPlugin.create()) // 启用内联解析
            .usePlugin(
                JLatexMathPlugin.create(28f){
                    it.inlinesEnabled(true) // 启用行内公式
                    it.blocksEnabled(true)   // 启用块级公式
                }
            )
            //.usePlugin(CodeCopyPlugin(holder.messageText)) // 添加我们的插件
            // 配置 HtmlPlugin：添加自定义 <img> 处理器
//            .usePlugin(HtmlPlugin.create(object : HtmlPlugin.HtmlConfigure {
//                override fun configureHtml(plugin: HtmlPlugin) {
//                    plugin.addHandler(
//                        CustomImgTagHandler(
//                            context = context,
//                            listener = imageClickListener,
//                            holder.messageText,
//                            message
//                        )
//                    )
//                    // 其他标签（如 <a> <b>）仍用默认处理器，无需额外操作
//                }
//            }))
            .usePlugin(HtmlPlugin.create()) // 支持HTML标签
            .usePlugin(StrikethroughPlugin.create()) // 支持删除线
            .usePlugin(TaskListPlugin.create(context)) // 支持任务列表
            .usePlugin(TablePlugin.create(context)) // 支持表格
            .usePlugin(GlideImagesPlugin.create(context)) // 使用Glide加载图片（需添加Glide依赖）
            //.usePlugin(ImageClickPlugin(WeakReference(imageClickListener)))
            .usePlugin(LinkifyPlugin.create()) // 自动识别 URL 并转换为链接
            // 添加自定义插件来替换默认的URLSpan
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder.linkResolver { view, url ->
                        // 自定义链接点击行为
                        if (view is TextView) {
                            // 示例：可以在这里添加链接点击统计等功能
                        }
                        // 使用默认行为打开链接
                        //android.text.util.Linkify.addLinks(view, android.text.util.Linkify.WEB_URLS)
                    }
                }

                override fun afterSetText(textView: TextView) {
                    super.afterSetText(textView)
                    // 处理文本设置后的操作，确保链接被正确应用
                    Log.e("ceshi","来到这里：${textView.text}")
                    val text = textView.text
                    if (text is Spannable) {
                        val urlSpans = text.getSpans(0, text.length, URLSpan::class.java)
                        Log.e("ceshi","0来到这里：${urlSpans.size}")

                        if (urlSpans.isEmpty()) {
                            // 尝试手动添加链接
                            Log.e("MarkwonDebug", "自定义插件: 未找到URLSpan，尝试手动添加链接")
                            android.text.util.Linkify.addLinks(textView, android.text.util.Linkify.WEB_URLS)

                            // 重新检查
                            val newUrlSpans = text.getSpans(0, text.length, URLSpan::class.java)
                            Log.e("MarkwonDebug", "自定义插件: 手动添加后URLSpan数量: ${newUrlSpans.size}")

                            for (newSpan in newUrlSpans) {
                                Log.e("MarkwonDebug", "自定义插件: 手动添加的URL: ${newSpan.url}")
                            }
                        } else {
                            for (span in urlSpans) {
                                Log.e("MarkwonDebug", "自定义插件: 找到URL: ${span.url}")
                                Log.e("ceshi","位置$curPosition,,大小${messageList.size}")
                                if (span.url.toString().contains("file.302.ai") && curPosition == messageList.size-1 && isChatNew){
                                    saveResource(context,span.url)
                                }
                                val start = text.getSpanStart(span)
                                val end = text.getSpanEnd(span)
                                val flags = text.getSpanFlags(span)
                                text.removeSpan(span)
                                text.setSpan(CustomUrlSpan(span.url), start, end, flags)
                            }
                        }


                    }
                }

            })
            .build()

        // 启用TextView的链接点击功能
        holder.messageText.movementMethod = android.text.method.LinkMovementMethod.getInstance()


        val fullText = message.trimIndent()
        val mFullText = StringObjectUtils.convertLatexFormat(fullText)
        //holder.deepLine.visibility = View.GONE
        if (message.contains("&&&&&&")){
            // 将Markdown渲染到TextView
            markwon.setMarkdown(holder.deepTextView, extractBeforeAmpersand(mFullText))
            markwon.setMarkdown(holder.messageText, AfterAmpersand(mFullText))
        }else{
            // 将Markdown渲染到TextView
            markwon.setMarkdown(holder.messageText, mFullText)
        }



    }

    fun extractBeforeAmpersand (input: String): String {
       // 查找 & 符号的位置
        val ampersandIndex = input.indexOf ("&&&&&&")
        // 如果找到 &，返回其前面的内容；否则返回原字符串
        return if (ampersandIndex != -1) {
            input.substring (0, ampersandIndex)
        } else {
            input
        }
    }

    fun AfterAmpersand (input: String): String {
        // 查找 & 符号的位置
        val ampersandIndex = input.indexOf ("&&&&&&")
        // 如果找到 &，返回其后面的内容；否则返回原字符串
        return if (ampersandIndex != -1 && ampersandIndex < input.length - 6) {
            input.substring (ampersandIndex + 6)
        } else {
            "" // 若没有 & 或 & 在最后，返回空字符串
        }
    }

    fun upDateIsNewChat(isNew:Boolean){
        isChatNew = isNew
    }
    fun upDataFileImage(fileName:String,fileSize:String,imageUrlLocalList:CopyOnWriteArrayList<String>){
        this.fileName = fileName
        this.fileSize = fileSize
        this.imageUrlLocalList = imageUrlLocalList
    }

    private fun addNewImageView(imageUrlLocal:String,holder: ChatViewHolder,context: Context) {
        Log.e("ceshi","添加视图")
        hashMapUrlHolder.put(imageUrlLocal,holder)
        //mHolder = holder
        val removableImageChatLayout = RemovableImageChatLayout(context,listenerOver=this@ChatAdapter).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 4.dpToPx()
            }
        }

        removableImageChatLayout.setOnClickListener {
            Log.e("ceshi","removableImageChatLayout")
            false
        }

        if (imageUrlLocal.contains("media.documents/")){
            removableImageChatLayout.setImageResource(imageUrlLocal,0,true,fileName, fileSize)
            // 添加到容器
            holder.fileChatLine.addView(removableImageChatLayout)
            isImageChatLine = false
        }else{
            removableImageChatLayout.setImageResource(imageUrlLocal,0,false,fileName, fileSize)
            // 添加到容器
            holder.imageChatLine.addView(removableImageChatLayout)
            isImageChatLine = true
        }






    }

    // 用 DiffUtil 更新数据
    fun updateData(newData: List<ChatMessage>) {
        val diffResult = DiffUtil.calculateDiff(ChatMessageDiffCallback(messageList, newData))
        messageList = newData
        diffResult.dispatchUpdatesTo(this)
    }

    private fun Int.dpToPx(): Int = (this * context.resources.displayMetrics.density).toInt()

    override fun onOverItemClick(wordPrintOverItem: Boolean) {

    }

    override fun onBackChatTool(backChatToolItem: BackChatToolItem) {

    }

    override fun onDeleteImagePosition(position: Int) {

    }

    override fun onPreImageClick(resUrl: String) {

    }

    override fun onImageBackClick(backImage: ImageBack) {
        Log.e("ChatAdapter","点击图片返回参数$backImage")
        val holder = hashMapUrlHolder.get(backImage.url)

        if (backImage.type == "imageLongClick"){
            //val options = mutableListOf("复制","分享","上传到档案库","添加到知识库")
            val options = mutableListOf(ContextCompat.getString(context, R.string.copy_toast_message))
            dialogUtils.setupPopupWindow(options,"imageLine",context)
            imageBack = backImage
            dialogUtils.showPopup(holder?.imageChatLine as View)
            if (isImageChatLine){
                dialogUtils.showPopup(holder?.imageChatLine as View)
            }else{
                dialogUtils.showPopup(holder?.fileChatLine as View)
            }
        }else{
            imageBack = backImage
            listener.onBackFunctionClick(ChatBackMessage(imageBack.url,false,"userImagePre",0,codeNameStr))
        }
        if (isImageChatLine){
            ViewAnimationUtils.performClickEffect(holder?.imageChatLine as View)
        }else{
            ViewAnimationUtils.performClickEffect(holder?.fileChatLine as View)
        }

    }

    fun upDataCancelBad(isCancel:Boolean) {
        if (isCancel){
            mHolder.badImage.setImageResource(R.drawable.icon_bad)
            notifyItemChanged(badPosition)
            //notifyDataSetChanged()
        }

    }


}