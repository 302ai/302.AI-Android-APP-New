package com.newAi302.app.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

// 自定义适配器：兼容 String 转 MutableList<String>
class StringToListAdapter : TypeAdapter<MutableList<String>>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: MutableList<String>?) {
        // 序列化时正常写入列表（转成 JSON 数组）
        out.beginArray()
        value?.forEach { out.value(it) }
        out.endArray()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): MutableList<String> {
        val list = mutableListOf<String>()
        // 反序列化时：如果是字符串，就包装成列表；如果是数组，就正常解析
        when (`in`.peek()) {
            JsonToken.STRING -> {
                // 旧数据是字符串（如 "file.txt"），转为列表 ["file.txt"]
                list.add(`in`.nextString())
            }
            JsonToken.BEGIN_ARRAY -> {
                // 新数据是数组（如 ["a.txt", "b.txt"]），正常解析
                `in`.beginArray()
                while (`in`.hasNext()) {
                    list.add(`in`.nextString())
                }
                `in`.endArray()
            }
            else -> {
                // 其他类型（如 null），返回空列表
                `in`.skipValue()
            }
        }
        return list
    }
}