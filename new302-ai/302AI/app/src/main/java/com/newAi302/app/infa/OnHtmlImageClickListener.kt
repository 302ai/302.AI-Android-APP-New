package com.newAi302.app.infa


interface OnHtmlImageClickListener {
    fun onImageClick(imagePath: String) // imagePath 为 <img src="xxx"> 中的 src 值
}