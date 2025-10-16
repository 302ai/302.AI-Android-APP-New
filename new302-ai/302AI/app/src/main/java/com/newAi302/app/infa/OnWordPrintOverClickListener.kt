package com.newAi302.app.infa

import com.newAi302.app.data.BackChatToolItem
import com.newAi302.app.data.ImageBack


interface OnWordPrintOverClickListener {
    fun onOverItemClick(wordPrintOverItem: Boolean)

    fun onBackChatTool(backChatToolItem: BackChatToolItem)

    fun onDeleteImagePosition(position:Int)

    fun onPreImageClick(resUrl:String)

    fun onImageBackClick(backImage:ImageBack)
}