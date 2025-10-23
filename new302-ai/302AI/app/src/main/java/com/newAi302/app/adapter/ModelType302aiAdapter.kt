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
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
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
class ModelType302aiAdapter(private val context:Context, private var modeTypeList: List<String>, private val listener: OnItemClickListener,private val isDefault:Boolean,
                            private var chatDatabase:ChatDatabase, private var lifecycleScope: LifecycleCoroutineScope
) :
    RecyclerView.Adapter<ModelType302aiAdapter.ChatViewHolder>() {
    // 记录上一次点击的 item 位置
    private var lastSelectedPosition = -1
    //private lateinit var dataStoreManager: DataStoreManager
    private var isLongPressed = false  // 标记是否已触发长按
    private lateinit var dialogUtils: DialogUtils
    private var nowPosition = 0
    private var isMoreSelect = false
    private var isDelete = false

    private var selectedList = mutableListOf<Int>()

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
        Log.e("ceshi","显示自定义位置$firstCustomizePosition,,$isCustomize")
        if (position == firstCustomizePosition && isCustomize) {
            holder.textTitleTv.visibility = View.VISIBLE
        } else {
            holder.textTitleTv.visibility = View.GONE
        }



        holder.settingModelTypSelectImage.setOnClickListener {
            holder.settingModelTypSelectImage.visibility = View.GONE
            holder.settingModelTypSelectedImage.visibility = View.VISIBLE
        }

        holder.settingModelTypSelectedImage.setOnClickListener {
            holder.settingModelTypSelectedImage.visibility = View.GONE
            holder.settingModelTypSelectImage.visibility = View.VISIBLE
        }




//        holder.btnDelete.setOnClickListener {
//            // 点击时执行动画效果
//            ViewAnimationUtils.performClickEffect(it)
//            onDeleteClickListener(position,"delete")
//            (holder.itemView as SwipeMenuLayout).smoothClose()
//        }
//        holder.btnEdit.setOnClickListener {
//            // 点击时执行动画效果
//            ViewAnimationUtils.performClickEffect(it)
//            onDeleteClickListener(position,"edit")
//            (holder.itemView as SwipeMenuLayout).smoothClose()
//        }






    }

    override fun getItemCount(): Int {
        return modeTypeList.size
    }


    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val modelTypeNameTv: TextView = itemView.findViewById(R.id.modelTypeNameTv)
        val settingModelTypSelectImage: ImageView = itemView.findViewById(R.id.settingModelTypSelectImage)
        val settingModelTypSelectedImage: ImageView = itemView.findViewById(R.id.settingModelTypSelectedImage)
        val textTitleTv: TextView = itemView.findViewById(R.id.textTitleTv)

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
}