package com.newAi302.app.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

// 定义DataStore名称
private const val IMAGE_URL_DATA_STORE = "image_url_mappings"

// 创建DataStore实例
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = IMAGE_URL_DATA_STORE
)

// 存储键
private val URL_MAPPINGS_KEY = stringPreferencesKey("url_mappings")

// 管理URL映射的单例类
class ImageUrlMapper(private val context: Context) {
    // 序列化工具
    private val json = Json { ignoreUnknownKeys = true }

    // 暴露映射关系的Flow，便于观察数据变化
    val urlMappingsFlow: Flow<Map<String, String>> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[URL_MAPPINGS_KEY] ?: "{}"
            json.decodeFromString<Map<String, String>>(jsonString)
        }

    // 保存单个URL映射（网络URL -> 本地URL）
    suspend fun saveUrlMapping(networkUrl: String, localUrl: String) {
        context.dataStore.edit { preferences ->
            // 先获取现有映射
            val currentMappings = getCurrentMappings(preferences)
            // 添加或更新新映射
            val newMappings = currentMappings.toMutableMap().apply {
                put(networkUrl, localUrl)
            }
            // 序列化并存储
            preferences[URL_MAPPINGS_KEY] = json.encodeToString(newMappings)
        }
    }

    // 批量保存URL映射
    suspend fun saveUrlMappings(mappings: Map<String, String>) {
        context.dataStore.edit { preferences ->
            val currentMappings = getCurrentMappings(preferences)
            val newMappings = currentMappings + mappings // 合并，新的会覆盖旧的
            preferences[URL_MAPPINGS_KEY] = json.encodeToString(newMappings)
        }
    }

    // 根据网络URL获取本地URL
    suspend fun getLocalUrl(networkUrl: String): String? {
        val currentMappings = urlMappingsFlow.first()
        return currentMappings[networkUrl]
    }

    // 根据本地URL查找网络URL
    suspend fun getNetworkUrl(localUrl: String): String? {
        val currentMappings = urlMappingsFlow.first()
        // 遍历映射关系，找到值为localUrl的键（网络URL）
        return currentMappings.entries.firstOrNull { it.value == localUrl }?.key
    }

    // 删除某个映射
    suspend fun deleteUrlMapping(networkUrl: String) {
        context.dataStore.edit { preferences ->
            val currentMappings = getCurrentMappings(preferences)
            val newMappings = currentMappings.toMutableMap().apply {
                remove(networkUrl)
            }
            preferences[URL_MAPPINGS_KEY] = json.encodeToString(newMappings)
        }
    }

    // 清空所有映射
    suspend fun clearAllMappings() {
        context.dataStore.edit { preferences ->
            preferences.remove(URL_MAPPINGS_KEY)
        }
    }

    // 从Preferences中获取当前映射
    private fun getCurrentMappings(preferences: Preferences): Map<String, String> {
        val jsonString = preferences[URL_MAPPINGS_KEY] ?: "{}"
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}