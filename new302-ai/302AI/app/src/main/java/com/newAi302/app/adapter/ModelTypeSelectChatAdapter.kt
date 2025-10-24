package com.newAi302.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.Transliterator.Position
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Database
import com.newAi302.app.R
import com.newAi302.app.data.ChatMessage
import com.newAi302.app.infa.OnItemClickListener
import com.newAi302.app.infa.OnItemSelectModelClickListener
import com.newAi302.app.room.ChatDatabase
import com.newAi302.app.room.ChatItemRoom
import com.newAi302.app.room.SelectModelData
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
class ModelTypeSelectChatAdapter(private val context:Context, private var modeTypeList: List<String>,
                                 private val isDefault:Boolean,
    // 定义函数类型参数（Lambda回调）：参数为位置和数据，无返回值
                                 private val onItemClick: (position: Int, data: String) -> Unit
                             ) :
    RecyclerView.Adapter<ModelTypeSelectChatAdapter.ChatViewHolder>() {
    // 记录上一次点击的 item 位置
    private var lastSelectedPosition = -1
    //private lateinit var dataStoreManager: DataStoreManager
    private var isLongPressed = false  // 标记是否已触发长按
    private lateinit var dialogUtils: DialogUtils
    private var nowPosition = 0
    private var isMoreSelect = false
    private var isDelete = false

    private var selectedList = mutableListOf<Int>()
    private var isSelect = false
    public var selectModelPosition = -1

    private lateinit var mHolder: ModelTypeSelectChatAdapter.ChatViewHolder
    private var hashMapUrlHolder = HashMap<Int, ModelTypeSelectChatAdapter.ChatViewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_model_type_select_chat_default, parent, false)
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
        Log.e("ceshi","模型列表显示模型：$modelType,,选中位置:$selectModelPosition")
        holder.modelTypeNameTv.text = modelType


        /*if (selectModelPosition == position ){
            Log.e("ceshi","来到")
            holder.settingModelTypSelectImage.visibility = View.GONE
            holder.settingModelTypSelectedImage.visibility = View.VISIBLE
        }*/

        /*holder.settingModelTypSelectImage.setOnClickListener {
            if (!isSelect){
                isSelect = true
                holder.settingModelTypSelectImage.visibility = View.GONE
                holder.settingModelTypSelectedImage.visibility = View.VISIBLE
            }

        }

        holder.settingModelTypSelectedImage.setOnClickListener {
            if (isSelect){
                isSelect = false
                holder.settingModelTypSelectedImage.visibility = View.GONE
                holder.settingModelTypSelectImage.visibility = View.VISIBLE
            }

        }*/

        // 根据位置设置背景颜色
        if (position == selectModelPosition) {
            // 当前点击的 item，设置为指定颜色
            holder.settingModelTypSelectImage.visibility = View.GONE
            holder.settingModelTypSelectedImage.visibility = View.VISIBLE
        }else{
            // 其他 item，设置为白色
            holder.settingModelTypSelectedImage.visibility = View.GONE
            holder.settingModelTypSelectImage.visibility = View.VISIBLE
        }

        holder.cueWordLeftLayout.setOnClickListener {
            hashMapUrlHolder.put(position,holder)
            // 保存上一次点击的位置
            val previousClickedPosition = selectModelPosition
            // 更新当前点击的位置
            selectModelPosition = position

            // 通知 RecyclerView 更新之前点击的 item 和当前点击的 item
            if (previousClickedPosition != -1) {
                notifyItemChanged(previousClickedPosition)
            }
            notifyItemChanged(position)
            // 执行Lambda表达式，传递位置和数据
            onItemClick(position, modelType)

            //listener.onItemClick(SelectModelData(position,modelType))
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
//        val time: TextView = itemView.findViewById(R.id.timeTv)
//        val btnDelete: Button = itemView.findViewById(R.id.mBtnDelete)
//        val contentLayout: View = itemView.findViewById(R.id.content_layout)
//        val btnEdit: Button = itemView.findViewById(R.id.mBtnEdit)
//        val collectImage: ImageView = itemView.findViewById(R.id.collectImage)
//        val selectImage: ImageView = itemView.findViewById(R.id.selectImage)
//        val selectedImage: ImageView = itemView.findViewById(R.id.selectedImage)
        val cueWordLeftLayout: View = itemView.findViewById(R.id.selectConst)
    }

    fun upDataMoreSelect(isMoreSelect:Boolean){
        this.isMoreSelect = isMoreSelect
    }
    fun upDataSelectModel(modelType:String,nowPosition: Int){
        this.selectModelPosition = nowPosition

    }
}