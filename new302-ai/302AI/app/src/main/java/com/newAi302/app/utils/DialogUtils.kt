package com.newAi302.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.newAi302.app.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * author :
 * e-mail :
 * time   : 2025/8/5
 * desc   :
 * version: 1.0
 */
class DialogUtils(private val onDeleteClickListener: (String) -> Unit) {
    private lateinit var popupWindow: PopupWindow
    private var mType = ""



    fun setupPopupWindow(options0:MutableList<String>,type:String,context:Context) {
        mType = type
        val popupView = LayoutInflater.from(context).inflate(R.layout.setting_popup_list, null)
        popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            isOutsideTouchable = true
            animationStyle = android.R.style.Animation_Dialog
        }

        val recyclerView = popupView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.popupRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
//        if (isModel){
//            recyclerView.layoutManager?.scrollToPosition(positionModeType)
//        }
        recyclerView.adapter = object : BaseQuickAdapter<String, BaseViewHolder>(
            R.layout.item_popup_select, options0
        ) {
            @SuppressLint("MissingInflatedId")
            override fun convert(holder: BaseViewHolder, item: String) {
                holder.setText(R.id.itemTextView, item)
                if (type == "leftHistory" && holder.position == 3){
                    holder.setTextColor(R.id.itemTextView,Color.RED)
                }
                when(type){
                    "leftHistory" -> {
                        holder.getView<ImageView>(R.id.chatListToolImage).visibility = View.GONE
                    }
                    "moreLine" -> {
                        when(holder.position){
                            /*0 -> {
                                holder.getView<ImageView>(R.id.chatListToolImage).setImageResource(R.drawable.icon_chat_share)
                            }
                            1 -> {
                                holder.getView<ImageView>(R.id.chatListToolImage).setImageResource(R.drawable.icon_chat_screenshot)
                            }
                            2 -> {
                                holder.getView<ImageView>(R.id.chatListToolImage).setImageResource(R.drawable.icon_choose_text)
                            }
                            3 -> {
                                holder.getView<ImageView>(R.id.chatListToolImage).setImageResource(R.drawable.icon_chat_kb)
                            }
                            4 -> {
                                holder.getView<ImageView>(R.id.chatListToolImage).setImageResource(R.drawable.icon_chat_add_kb)
                            }*/
                            0 -> {
                                holder.getView<ImageView>(R.id.chatListToolImage).setImageResource(R.drawable.icon_chat_screenshot)
                            }
                            1 -> {
                                holder.getView<ImageView>(R.id.chatListToolImage).setImageResource(R.drawable.icon_choose_text)
                            }

                        }
                        //不能使用popupView.findViewById<ImageView>(R.id.chatListToolImage)，查找为空
                        holder.getView<ImageView>(R.id.chatListToolImage).visibility = View.VISIBLE
                    }
                    "meChatList" -> {
                        when(holder.position){
                            0 -> {
                                holder.getView<ImageView>(R.id.chatListToolImage).setImageResource(R.drawable.icon_chat_me_copy)
                            }
                            1 -> {
                                holder.getView<ImageView>(R.id.chatListToolImage).setImageResource(R.drawable.icon_chat_me_edit)
                            }
                            2 -> {
                                holder.getView<ImageView>(R.id.chatListToolImage).setImageResource(R.drawable.icon_new_again1)
                            }
                        }
                    }
                    "ResourceActivity" -> {
                        holder.getView<ImageView>(R.id.chatListToolImage).visibility = View.GONE
                    }
                    "imageLine" -> {
                        when(holder.position){
                            0 -> {
                                holder.getView<ImageView>(R.id.chatListToolImage).setImageResource(R.drawable.icon_chat_me_copy)
                            }
                            1 -> {
                                holder.getView<ImageView>(R.id.chatListToolImage).setImageResource(R.drawable.icon_chat_share)
                            }
                            2 -> {
                                holder.getView<ImageView>(R.id.chatListToolImage).setImageResource(R.drawable.icon_chat_kb)
                            }
                            3 -> {
                                holder.getView<ImageView>(R.id.chatListToolImage).setImageResource(R.drawable.icon_chat_add_kb)
                            }
                        }
                    }

                }
            }
        }.apply {
            setOnItemClickListener { _, _, position ->
                popupWindow.dismiss()
                when(type){
                    "leftHistory" -> {
                        when(position){
                            0 -> {
                                onDeleteClickListener("重命名")
                            }
                            1 -> {
                                onDeleteClickListener("多选")
                            }
                            2 -> {
                                onDeleteClickListener("收藏")
                            }
                            3 -> {
                                onDeleteClickListener("删除")
                            }
                        }
                    }
                    "chatRobot" -> {

                    }
                    "moreLine" -> {
                        when(position){
                            /*0 -> {
                                onDeleteClickListener("分享")
                            }
                            1 -> {
                                onDeleteClickListener("截图")
                            }
                            2 -> {
                                onDeleteClickListener("选择文本")
                            }
                            3 -> {
                                onDeleteClickListener("上传到档案库")
                            }
                            4 -> {
                                onDeleteClickListener("添加到知识库")
                            }*/
                            0 -> {
                                onDeleteClickListener("截图")
                            }
                            1 -> {
                                onDeleteClickListener("选择文本")
                            }
                        }
                    }
                    "meChatList" -> {
                        when(position){
                            0 -> {
                                onDeleteClickListener("用户复制")
                            }
                            1 -> {
                                onDeleteClickListener("用户编辑")
                            }
                            2 -> {
                                onDeleteClickListener("用户重试")
                            }
                        }
                    }
                    "settingTitleGetModelTypeList" -> {
                        when(position){
                            0 -> {
                                //onDeleteClickListener("第一次")
                                onDeleteClickListener(ContextCompat.getString(context, R.string.setting_preferences_build_title_model_time_one_message))
                            }
                            1 -> {
                                //onDeleteClickListener("每一次")
                                onDeleteClickListener(ContextCompat.getString(context, R.string.setting_preferences_build_title_model_time_every_message))
                            }
                        }
                    }
                    "languageTypeList" -> {
                        when(position){
                            0 -> {
                                onDeleteClickListener("中文")
                                //onDeleteClickListener(ContextCompat.getString(context, R.string.language_ch_message))
                            }
                            1 -> {
                                onDeleteClickListener("English")
                                //onDeleteClickListener(ContextCompat.getString(context, R.string.language_en_message))
                            }
                            2 -> {
                                onDeleteClickListener("日本語")
                                //onDeleteClickListener(ContextCompat.getString(context, R.string.language_ja_message))
                            }
                        }
                    }
                    "ResourceActivity" -> {
                        when(position){
                            0 -> {
                                onDeleteClickListener("定位到聊天位置")
                            }
                            1 -> {
                                onDeleteClickListener("复制")
                            }
                            2 -> {
                                onDeleteClickListener("分享")
                            }
                            3 -> {
                                onDeleteClickListener("保存到相册")
                            }
                            4 -> {
                                onDeleteClickListener("上传到档案库")
                            }
                            5 -> {
                                onDeleteClickListener("添加到知识库")
                            }
                        }
                    }
                    "imageLine" -> {
                        when(position){
                            0 -> {
                                onDeleteClickListener("用户图片复制")
                            }
                            1 -> {
                                onDeleteClickListener("用户图片分享")
                            }
                            2 -> {
                                onDeleteClickListener("用户图片上传到档案库")
                            }
                            3 -> {
                                onDeleteClickListener("用户图片添加到知识库")
                            }
                        }
                    }
                }

            }
        }
    }

    fun showPopup(anchorView: View) {
        when(mType){
            "leftHistory" -> {
                popupWindow.showAsDropDown(
                    anchorView,
                    -(anchorView.width - popupWindow.width) / 2+450,
                    8
                )
            }
            "moreLine" -> {
                popupWindow.showAsDropDown(
                    anchorView,
                    -(anchorView.width - popupWindow.width) / 2,
                    8
                )
            }
            "meChatList" -> {
                popupWindow.showAsDropDown(
                    anchorView,
                    -(anchorView.width - popupWindow.width) / 2,
                    8
                )
            }
            "settingTitleGetModelTypeList" -> {
                popupWindow.showAsDropDown(
                    anchorView,
                    -(anchorView.width - popupWindow.width) / 2,
                    8
                )
            }
            "languageTypeList" -> {
                popupWindow.showAsDropDown(
                    anchorView,
                    -(anchorView.width - popupWindow.width) / 2,
                    8
                )
            }
            "ResourceActivity" -> {
                popupWindow.showAsDropDown(
                    anchorView,
                    -(anchorView.width - popupWindow.width) / 2,
                    8
                )
            }
            "imageLine" -> {
                popupWindow.showAsDropDown(
                    anchorView,
                    -(anchorView.width - popupWindow.width) / 2,
                    8
                )
            }
        }

    }





}