package com.newAi302.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Database
import com.newAi302.app.R
import com.newAi302.app.data.ChatMessage
import com.newAi302.app.infa.OnItemClickListener
import com.newAi302.app.room.ChatDatabase
import com.newAi302.app.room.ChatItemRoom
import com.newAi302.app.utils.DialogUtils
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
class ModelTypeManager302aiAdapter(private val context:Context, private var modeTypeList: List<String>, private var chatDatabase:ChatDatabase, private var lifecycleScope: LifecycleCoroutineScope,
    // 定义函数类型参数（Lambda回调）：参数为位置和数据，无返回值
                                   private val onItemClick: (position: Int, data: String) -> Unit) :
    RecyclerView.Adapter<ModelTypeManager302aiAdapter.ChatViewHolder>() {
    // 记录上一次点击的 item 位置
    private var lastSelectedPosition = -1
    //private lateinit var dataStoreManager: DataStoreManager
    private var isLongPressed = false  // 标记是否已触发长按
    private lateinit var dialogUtils: DialogUtils
    private var nowPosition = 0
    private var isMoreSelect = false
    private var isDelete = false

    private var selectedList = mutableListOf<Int>()
    private var isShow = false
    // 缓存每个modelType对应的isCustomize状态（key: modelType, value: isCustomize）
    private val customizeStatusMap = mutableMapOf<String, Boolean>()
    // 记录第一个isCustomize = true的Item位置（初始为-1，代表无符合条件的Item）
    private var firstCustomizePosition = -1

    init {
        loadCustomizeStatus()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_model_type_manager_select_default, parent, false)
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
        val modelType = modeTypeList[position]
        Log.e("ceshi","历史列表显示标题：$modelType")
        holder.modelTypeNameTv.text = modelType

        // 默认隐藏，避免复用导致的显示异常
        holder.textTitleTv.visibility = View.GONE

        // 从缓存中获取当前Item的isCustomize状态
        val isCustomize = customizeStatusMap[modelType] ?: false

        // 仅当：当前位置是第一个符合条件的位置，且状态为true时，才显示标题
        if (position == firstCustomizePosition && isCustomize) {
            holder.textTitleTv.visibility = View.VISIBLE
        } else {
            holder.textTitleTv.visibility = View.GONE
        }


        // 关键：强制刷新ItemView的布局，解决高度计算错误  处理更新数据时会出现空白区域
        holder.swipeLayout.post {
            try {
                val field = SwipeMenuLayout::class.java.getDeclaredField("mHeight")
                field.isAccessible = true
                field.set(holder.swipeLayout, 0) // 重置缓存高度为0
                holder.swipeLayout.requestLayout() // 重新测量
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        /*lifecycleScope.launch(Dispatchers.IO) {
            val modelData = chatDatabase.chatDao().getModelById(modelType)
            if (modelData != null){
                val isCustomize = modelData.isCustomize
                lifecycleScope.launch(Dispatchers.Main) {
                    if (isCustomize && !isShow){
                        isShow = true
                        holder.textTitleTv.visibility = View.VISIBLE
                    }else{
                        holder.textTitleTv.visibility = View.GONE
                    }
                }

            }else{
                lifecycleScope.launch(Dispatchers.Main) {
                    holder.textTitleTv.visibility = View.GONE
                }

            }
        }*/
        /*if (true && !isShow){
            isShow = true
            holder.textTitleTv.visibility = View.VISIBLE
        }*/





//        holder.btnDelete.setOnClickListener {
//            // 点击时执行动画效果
//            ViewAnimationUtils.performClickEffect(it)
//            onDeleteClickListener(position,"delete")
//            (holder.itemView as SwipeMenuLayout).smoothClose()
//        }
//        holder.btnEdit.setOnClickListener {
            // 点击时执行动画效果
//            ViewAnimationUtils.performClickEffect(it)
//            onDeleteClickListener(position,"edit")
//            (holder.itemView as SwipeMenuLayout).smoothClose()
//        }

        holder.selectConst.setOnClickListener {
            // 点击时执行动画效果
            ViewAnimationUtils.performClickEffect(it)
            onItemClick(position, modelType)
        }

        holder.deleteFrame.setOnClickListener {
            if (holder.textTitleTv.visibility == View.VISIBLE){
                isShow = false
                // 点击时执行动画效果
                onItemClick(position, "Delete")
            }else{
                // 点击时执行动画效果
                onItemClick(position, "Delete1")
            }

            ViewAnimationUtils.performClickEffect(it)
            (holder.itemView as SwipeMenuLayout).smoothClose()
        }






    }

    override fun getItemCount(): Int {
        return modeTypeList.size
    }


    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val modelTypeNameTv: TextView = itemView.findViewById(R.id.modelTypeNameTv)
        val textTitleTv: TextView = itemView.findViewById(R.id.textTitleTv)
        val settingModelTypSelectLine: LinearLayout = itemView.findViewById(R.id.settingModelTypSelectLine)
        val deleteFrame: FrameLayout = itemView.findViewById(R.id.deleteFrame)
        val selectConst: ConstraintLayout = itemView.findViewById(R.id.selectConst)
//        val settingModelTypSelectedImage: ImageView = itemView.findViewById(R.id.settingModelTypSelectedImage)
//        val time: TextView = itemView.findViewById(R.id.timeTv)
//        val btnDelete: Button = itemView.findViewById(R.id.mBtnDelete)
//        val contentLayout: View = itemView.findViewById(R.id.content_layout)
//        val btnEdit: Button = itemView.findViewById(R.id.mBtnEdit)
//        val collectImage: ImageView = itemView.findViewById(R.id.collectImage)
//        val selectImage: ImageView = itemView.findViewById(R.id.selectImage)
//        val selectedImage: ImageView = itemView.findViewById(R.id.selectedImage)
        val swipeLayout: SwipeMenuLayout = itemView.findViewById(R.id.swipeLayout)
    }

    fun upDataMoreSelect(isMoreSelect:Boolean){
        this.isMoreSelect = isMoreSelect
    }

    // 调用时机：Adapter初始化时、modeTypeList数据变化时
    fun loadCustomizeStatus() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 清空缓存和位置记录
            customizeStatusMap.clear()
            firstCustomizePosition = -1

            // 遍历所有Item，查询并缓存isCustomize状态
            for ((index, modelType) in modeTypeList.withIndex()) {
                val modelData = chatDatabase.chatDao().getModelById(modelType)
                val isCustomize = modelData?.isCustomize ?: false
                customizeStatusMap[modelType] = isCustomize

                // 找到第一个isCustomize = true的位置（仅记录一次）
                if (isCustomize && firstCustomizePosition == -1) {
                    firstCustomizePosition = index
                }
            }

            // 数据准备完成后，刷新列表（在主线程执行）
            launch(Dispatchers.Main) {
                notifyDataSetChanged()
            }
        }
    }

    // 新增更新数据的方法
    fun updateData(newList: List<String>) {
        modeTypeList = newList
        loadCustomizeStatus() // 重新计算状态和位置
    }
}