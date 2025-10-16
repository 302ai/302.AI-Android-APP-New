package com.newAi302.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Database
import com.newAi302.app.R
import com.newAi302.app.data.ChatMessage
import com.newAi302.app.infa.OnItemClickListener
import com.newAi302.app.room.ChatDatabase
import com.newAi302.app.room.ChatItemRoom
import com.newAi302.app.utils.DialogUtils
import com.newAi302.app.utils.TimeUtils
import com.newAi302.app.utils.ViewAnimationUtils
import com.newAi302.app.utils.VoiceToTextUtils
import com.mcxtzhang.swipemenulib.SwipeMenuLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException

/**
 * author :
 * e-mail : "time/{r/p}"
 * time   : 2025/4/15
 * desc   :
 * version: 1.0
 */
class HomeMessageAdapter(private val context:Context,private var chatList: List<ChatItemRoom>, private val listener: OnItemClickListener, private val onDeleteClickListener: (Int,String) -> Unit,) :
    RecyclerView.Adapter<HomeMessageAdapter.ChatViewHolder>() {
    // 记录上一次点击的 item 位置
    private var lastSelectedPosition = -1
    //private lateinit var dataStoreManager: DataStoreManager
    private var isLongPressed = false  // 标记是否已触发长按
    private lateinit var dialogUtils: DialogUtils
    private var nowPosition = 0
    private var isMoreSelect = false
    private var isDelete = false

    private var selectedList = mutableListOf<Int>()

    private var isShowTimeTag = false
    private var isShowTimeOneTag = false

    private var isSelectDelect = false
    private var hashMapUrlHolder = HashMap<Int, HomeMessageAdapter.ChatViewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_list_item, parent, false)
        //dataStoreManager = DataStoreManager(context)
        // 仅在打开时读取一次数据
        /*CoroutineScope(Dispatchers.IO).launch {
            val data = dataStoreManager.readLastPosition.first()
            data?.let {
                lastSelectedPosition = it
            }
        }*/
        return ChatViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatItem = chatList[position]
        Log.e("ceshi","历史列表显示：$chatItem")
        Log.e("ceshi","历史列表显示时间：${chatItem.time}")//2025-08-12 16:54:59
        holder.title.text = chatItem.title
        var timeTag = "今日"
        /*if (chatItem.title=="身份揭秘"){
            timeTag = TimeUtils.getTimeTag("2025-08-10 16:54:59",TimeUtils.getCurrentDateTime())
        }else if(chatItem.title == "你"){
            timeTag = TimeUtils.getTimeTag("2025-08-11 16:54:59",TimeUtils.getCurrentDateTime())
        }else if (chatItem.title == "哈哈"){
            timeTag = TimeUtils.getTimeTag("2025-08-11 16:54:59",TimeUtils.getCurrentDateTime())
        }else{
            timeTag = TimeUtils.getTimeTag(chatItem.time,TimeUtils.getCurrentDateTime())
        }*/
        timeTag = TimeUtils.getTimeTag(chatItem.time,TimeUtils.getCurrentDateTime())
        Log.e("ceshi","显示时间$timeTag,,$isShowTimeTag,,$isShowTimeOneTag")
        when(timeTag){
            "今日" -> {
                holder.time.visibility = View.GONE
            }
            "昨天" -> {
                if (!isShowTimeTag){
                    holder.time.visibility = View.VISIBLE
                    holder.time.text = getString(context,R.string.yesterday_message)
                    isShowTimeTag = true
                }

            }
            "更早" -> {
                if (!isShowTimeOneTag){
                    holder.time.visibility = View.VISIBLE
                    holder.time.text = getString(context,R.string.earlier_message)
                    isShowTimeOneTag = true
                }

            }
        }
        //holder.time.text = chatItem.time

        if (isMoreSelect){
            holder.collectImage.visibility = View.GONE
            holder.selectImage.visibility = View.VISIBLE
        }else{
            holder.selectImage.visibility = View.GONE
            holder.selectedImage.visibility = View.GONE
            if (chatItem.isCollected){
                holder.collectImage.visibility = View.VISIBLE
            }else{
                holder.collectImage.visibility = View.GONE
            }
        }
        /*holder.selectImage.setOnClickListener {
            val currentPos = holder.adapterPosition // 获取实时位置
            holder.selectImage.visibility = View.GONE
            holder.selectedImage.visibility = View.VISIBLE
            selectedList.add(currentPos)
            listener.onDeleteClick(selectedList)
        }
        holder.selectedImage.setOnClickListener {
            val currentPos = holder.adapterPosition // 获取实时位置
            holder.selectImage.visibility = View.VISIBLE
            holder.selectedImage.visibility = View.GONE
            selectedList.remove(currentPos)
            listener.onDeleteClick(selectedList)

        }*/

        holder.collectImage.setOnClickListener {
            holder.collectImage.visibility = View.GONE
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            onDeleteClickListener(position,"unCollect")
        }

        //因为使用了监听长按监听后这个监听不起作用引用了下方的监听OnTouchListener
        /*holder.contentLayout.setOnClickListener {
            Log.e("ceshi","点击了")
            // 保存上一次点击的位置
            val previousClickedPosition = lastSelectedPosition
            // 更新当前点击的位置
            lastSelectedPosition = position
//            CoroutineScope(Dispatchers.IO).launch {
//                dataStoreManager.saveLastPosition(lastSelectedPosition)
//            }

            // 通知 RecyclerView 更新之前点击的 item 和当前点击的 item
            if (previousClickedPosition != -1 && previousClickedPosition<chatList.size-1) {
                notifyItemChanged(previousClickedPosition)
            }
            notifyItemChanged(position)

            listener.onItemClick(chatItem)
        }*/
        holder.contentLayout.setOnTouchListener(View.OnTouchListener { v, event ->
            Log.e("ceshi", "WebView按下事件0")
            // 处理触摸事件，例如记录点击位置等
            false // 返回false表示事件没有被完全消费，可以继续传递到WebView内部处理（如点击链接）
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
                    Log.e("ceshi", "WebView抬起事件$isMoreSelect,,$isLongPressed,,$isSelectDelect")
                    if (!isMoreSelect){
                        if (!isLongPressed){
                            // 保存上一次点击的位置
                            val previousClickedPosition = lastSelectedPosition
                            // 更新当前点击的位置
                            lastSelectedPosition = position

                            // 通知 RecyclerView 更新之前点击的 item 和当前点击的 item
                            if (previousClickedPosition != -1 && previousClickedPosition<chatList.size-1) {
                                notifyItemChanged(previousClickedPosition)
                            }
                            notifyItemChanged(position)

                            listener.onItemClick(chatItem)
                        }
                    }else{
                        if (holder.selectImage.visibility == View.VISIBLE){
                            isSelectDelect = true
                            val currentPos = holder.adapterPosition // 获取实时位置
                            holder.selectImage.visibility = View.GONE
                            holder.selectedImage.visibility = View.VISIBLE
                            selectedList.add(currentPos)
                            listener.onDeleteClick(selectedList)
                        }else{
                            isSelectDelect = false
                            val currentPos = holder.adapterPosition // 获取实时位置
                            holder.selectImage.visibility = View.VISIBLE
                            holder.selectedImage.visibility = View.GONE
                            selectedList.remove(currentPos)
                            listener.onDeleteClick(selectedList)
                        }
                    }

                    false  // 让事件继续传递给WebView内部处理
                }

                else -> {
                    // 其他事件（如ACTION_MOVE）不处理
                    false
                }
            }
        })

        /*holder.btnDelete.setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            timeTag = TimeUtils.getTimeTag(chatList[position].time,TimeUtils.getCurrentDateTime())
            isShowTimeTag = false
            isShowTimeOneTag = false

            /*when(timeTag){
                "今日" -> {
                    isShowTimeTag = false
                    isShowTimeOneTag = false
                }
                "昨天" -> {
                    if (holder.time.visibility == View.VISIBLE){
                        isShowTimeTag = false
                    }

                }
                "更早" -> {
                    if (holder.time.visibility == View.VISIBLE){
                        isShowTimeOneTag = false
                    }

                }
            }*/
            onDeleteClickListener(position,"delete")
            (holder.itemView as SwipeMenuLayout).smoothClose()
        }*/
        /*holder.btnEdit.setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            onDeleteClickListener(position,"edit")
            (holder.itemView as SwipeMenuLayout).smoothClose()
        }*/

        holder.contentLayout.setOnLongClickListener {
            isLongPressed = true
            // 长按事件触发时执行的逻辑
            Log.e("ceshi","按钮被长按了")
            ViewAnimationUtils.performClickEffect(it)
            nowPosition = position
            hashMapUrlHolder.put(nowPosition,holder)
            onDeleteClickListener(position,"longPressed")

            val options = mutableListOf(ContextCompat.getString(context, R.string.rename_message),
                getString(context, R.string.more_select_message),
                getString(context, R.string.collect_message),
                getString(context, R.string.delete_message))
            dialogUtils.setupPopupWindow(options,"leftHistory",context)
            dialogUtils.showPopup(it)
            // 返回true表示消费了该事件，不会再触发点击事件
            // 返回false则会在长按结束后触发点击事件
            false
        }

        dialogUtils = DialogUtils {
            Log.e("ceshi","弹窗返回$it")
            when(it){
                "重命名" -> {
                    onDeleteClickListener(nowPosition,"edit")
                }
                "多选" -> {
                    onDeleteClickListener(nowPosition,"moreSelect")
                    isMoreSelect = true
                }
                "收藏" -> {
                    onDeleteClickListener(nowPosition,"collect")
                }
                "删除" -> {
                    timeTag = TimeUtils.getTimeTag(chatList[nowPosition].time,TimeUtils.getCurrentDateTime())
                    val mHolder = hashMapUrlHolder.get(nowPosition)
                    Log.e("ceshi","删除历史$timeTag")
                    Log.e("ceshi","00删除历史$timeTag,,${mHolder?.time?.visibility == View.VISIBLE}")
                    isShowTimeTag = false
                    isShowTimeOneTag = false
                    /*when(timeTag){
                        "今日" -> {
                            isShowTimeTag = false
                            isShowTimeOneTag = false
                        }
                        "昨天" -> {
                            if (mHolder?.time?.visibility == View.VISIBLE){
                                Log.e("ceshi","删除历史")
                                isShowTimeTag = false
                            }

                        }
                        "更早" -> {
                            if (mHolder?.time?.visibility == View.VISIBLE){
                                isShowTimeOneTag = false
                            }

                        }
                    }*/
                    onDeleteClickListener(nowPosition,"delete")
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    // 更新数据集的方法
    fun updateData(newChatList: List<ChatItemRoom>) {
        chatList = newChatList
        notifyDataSetChanged() // 通知适配器数据集已改变
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleTv)
        val time: TextView = itemView.findViewById(R.id.timeTv)
        //val btnDelete: Button = itemView.findViewById(R.id.mBtnDelete)
        val contentLayout: View = itemView.findViewById(R.id.content_layout)
        //val btnEdit: Button = itemView.findViewById(R.id.mBtnEdit)
        val collectImage: ImageView = itemView.findViewById(R.id.collectImage)
        val selectImage: ImageView = itemView.findViewById(R.id.selectImage)
        val selectedImage: ImageView = itemView.findViewById(R.id.selectedImage)
    }

    fun upDataMoreSelect(isMoreSelect:Boolean){
        this.isMoreSelect = isMoreSelect
    }
}