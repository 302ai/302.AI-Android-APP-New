package com.newAi302.app.utils

import android.content.Context
import android.content.res.AssetManager

/**
 * author :
 * e-mail :
 * time   : 2025/4/27
 * desc   :
 * version: 1.0
 */
object JsonOneUtils {

    fun loadJSONFromAsset(fileName: String,context: Context): String? {
        return try {
            val assetManager: AssetManager = context.assets
            val inputStream = assetManager.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }



}