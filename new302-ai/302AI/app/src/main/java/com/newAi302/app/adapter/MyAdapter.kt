package com.newAi302.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.newAi302.app.R

class MyAdapter : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 有具体的控件引用等
    }
    // 重写 onCreateViewHolder、onBindViewHolder 等方法
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // 创建并返回 MyViewHolder
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_popup_select, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // 绑定数据逻辑
    }
}