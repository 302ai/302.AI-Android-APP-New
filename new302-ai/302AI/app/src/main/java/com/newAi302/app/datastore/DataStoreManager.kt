package com.newAi302.app.datastore
import android.annotation.SuppressLint
import android.content.Context
import android.media.Image
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2025/4/14
 * desc   :
 * version: 1.0
 */
// 定义 DataStore 的名称
private const val DATA_STORE_NAME = "my_data_store"

// 扩展属性，用于获取 DataStore 实例
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

class DataStoreManager(private val context: Context) {

    // 定义一个偏好键
    private val API_KEY = stringPreferencesKey("api_key")
    private val CHAT_LIST_NUMBER = intPreferencesKey("chat_list_number")
    private val IS_CHAT = booleanPreferencesKey("is_chat")
    private val SERVICE_URL = stringPreferencesKey("service_url")
    private val MODEL_TYPE = stringPreferencesKey("model_type")
    private val LAST_MODEL_TYPE = stringPreferencesKey("last_model_type")
    private val LAST_SELECTED_POSITION = intPreferencesKey("last_selected_position")
    private val IMAGE_URL = stringPreferencesKey("image_url")
    private val CUE_WORDS = stringPreferencesKey("cue_words")
    private val CUE_WORDS_SWITCH = booleanPreferencesKey("cue_words_switch")

    private val CLEAR_WORDS_SWITCH = booleanPreferencesKey("clear_words_switch")
    private val PRE_SWITCH = booleanPreferencesKey("pre_switch")
    private val EXTRACT_SWITCH = booleanPreferencesKey("extract_switch")
    private val OFFICIAL_WORDS_SWITCH = booleanPreferencesKey("official_words_switch")
    private val SEARCH_SWITCH = booleanPreferencesKey("search_switch")

    private val APP_EMOJIS = stringPreferencesKey("app_emojis")
    private val CUSTOMIZE_API_KEY = stringPreferencesKey("customize_api_key")
    private val CUSTOMIZE_SERVICE_URL = stringPreferencesKey("customize_service_url")
    private val CUSTOMIZE_MODEL_ID = stringPreferencesKey("customize_model_id")
    private val SERVICE_PROVIDER = stringPreferencesKey("service_provider")

    private val OPEN_AI_API_KEY = stringPreferencesKey("open_ai_api_key")
    private val ANTHROPIC_API_KEY = stringPreferencesKey("anthropic_api_key")

    private val IS_CHANGE_MODEL_SETTING = booleanPreferencesKey("is_change_Model_Setting")

    private val USER_NAME = stringPreferencesKey("user_name")
    private val USER_EMAIL = stringPreferencesKey("user_email")
    private val USER_BALANCE = doublePreferencesKey("user_balance")

    private val BUILD_TITLE_SWITCH = booleanPreferencesKey("build_title_switch")
    private val USE_TRACELESS_SWITCH = booleanPreferencesKey("use_traceless_switch")
    private val SLIDE_BOTTOM_SWITCH = booleanPreferencesKey("slide_bottom_switch")

    private val SEARCH_SERVICE_TYPE = stringPreferencesKey("search_service_type")

    private val CHAT_DEFAULT_MODEL_TYPE = stringPreferencesKey("chat_default_model_type")
    private val BUILD_TITLE_MODEL_TYPE = stringPreferencesKey("build_title_model_type")
    private val BUILD_TITLE_TIME = stringPreferencesKey("chat_default_time")

    private val TEMPERATURE_VALUE = doublePreferencesKey("temperature_value")

    private val TEMPORARY_MODEL_TYPE = stringPreferencesKey("temporary_model_type")
    private val TEMPORARY_CHAT_TITLE = stringPreferencesKey("temporary_chat_title")

    // 存储键
    private val MODEL_LIST_KEY = stringPreferencesKey("model_list_key")
    private val CUSTOMIZE_MODEL_LIST_KEY = stringPreferencesKey("customize_model_list_key")
    // 序列化工具
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: DataStoreManager? = null

        fun getInstance(context: Context): DataStoreManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataStoreManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    // 暴露映射关系的Flow，便于观察数据变化
    val modelListFlow: Flow<MutableList<String>> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[MODEL_LIST_KEY] ?: "[]"
            json.decodeFromString<MutableList<String>>(jsonString)
        }
    suspend fun saveModelList(newModelList: MutableList<String>) {
        context.dataStore.edit { preferences ->
            // 序列化并存储
            preferences[MODEL_LIST_KEY] = json.encodeToString(newModelList)
        }
    }

    val customizeModelListFlow: Flow<MutableList<String>> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[CUSTOMIZE_MODEL_LIST_KEY] ?: "[]"
            json.decodeFromString<MutableList<String>>(jsonString)
        }
    suspend fun saveCustomizeModelList(newModelList: MutableList<String>) {
        context.dataStore.edit { preferences ->
            // 序列化并存储
            preferences[CUSTOMIZE_MODEL_LIST_KEY] = json.encodeToString(newModelList)
        }
    }
    // 添加删除元素的方法
    suspend fun deleteFromCustomizeModelList(target: String) {
        // 1. 获取当前列表（通过 flow 收集一次当前值）
        val currentList = customizeModelListFlow.first() // 注意：first() 会挂起并获取当前值

        // 2. 创建新列表（避免直接修改原列表，确保线程安全）
        val newList = currentList.toMutableList()

        // 3. 移除目标元素（remove 会删除第一个匹配的元素）
        newList.remove(target)

        // 4. 保存修改后的列表
        saveCustomizeModelList(newList)
    }




    // 保存数据到 DataStore
    suspend fun saveData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = data
        }
    }

    suspend fun saveChatListNumber(number:Int){
        context.dataStore.edit { preferences ->
            preferences[CHAT_LIST_NUMBER] = number
        }
    }

    suspend fun saveServiceUrl(data: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVICE_URL] = data
        }
    }

    suspend fun saveModelType(data: String) {
        context.dataStore.edit { preferences ->
            preferences[MODEL_TYPE] = data
        }
    }

    suspend fun saveLastModelType(data: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_MODEL_TYPE] = data
        }
    }

    suspend fun saveLastPosition(data: Int) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SELECTED_POSITION] = data
        }
    }

    suspend fun saveChatIs(data: String){
        context.dataStore.edit { preferences ->
            preferences[MODEL_TYPE] = data
        }
    }

    suspend fun saveImageUrl(data: String) {
        context.dataStore.edit { preferences ->
            preferences[IMAGE_URL] = data
        }
    }

    suspend fun saveCueWords(data: String) {
        context.dataStore.edit { preferences ->
            preferences[CUE_WORDS] = data
        }
    }

    suspend fun saveCueWordsSwitch(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CUE_WORDS_SWITCH] = data
        }
    }

    suspend fun saveOfficialWordsSwitch(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[OFFICIAL_WORDS_SWITCH] = data
        }
    }

    suspend fun saveClearWordsSwitch(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CLEAR_WORDS_SWITCH] = data
        }
    }

    suspend fun savePreSwitch(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PRE_SWITCH] = data
        }
    }

    suspend fun saveExtractSwitch(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[EXTRACT_SWITCH] = data
        }
    }

    suspend fun saveSearchSwitch(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SEARCH_SWITCH] = data
        }
    }

    suspend fun saveAppEmojisData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_EMOJIS] = data
        }
    }

    suspend fun saveCustomizeKeyData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOMIZE_API_KEY] = data
        }
    }

    suspend fun saveCustomizeServiceUrlData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOMIZE_SERVICE_URL] = data
        }
    }

    suspend fun saveCustomizeModelIdData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOMIZE_MODEL_ID] = data
        }
    }

    suspend fun saveServiceProviderData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVICE_PROVIDER] = data
        }
    }

    suspend fun saveIsChangeModelSetting(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_CHANGE_MODEL_SETTING] = data
        }
    }

    suspend fun saveOpenAiKeyData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[OPEN_AI_API_KEY] = data
        }
    }

    suspend fun saveAnthropiocKeyData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[ANTHROPIC_API_KEY] = data
        }
    }

    suspend fun saveUserName(data: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = data
        }
    }

    suspend fun saveUserBalance(data: Double) {
        context.dataStore.edit { preferences ->
            preferences[USER_BALANCE] = data
        }
    }

    suspend fun saveUserEmail(data: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL] = data
        }
    }

    suspend fun saveTemporaryModelType(data: String) {
        context.dataStore.edit { preferences ->
            preferences[TEMPORARY_MODEL_TYPE] = data
        }
    }

    suspend fun saveTemporaryChatTitle(data: String) {
        context.dataStore.edit { preferences ->
            preferences[TEMPORARY_CHAT_TITLE] = data
        }
    }

    suspend fun saveBuildTitleSwitch(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BUILD_TITLE_SWITCH] = data
        }
    }

    suspend fun saveSlideBottomSwitch(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SLIDE_BOTTOM_SWITCH] = data
        }
    }

    suspend fun saveSearchServiceTypeData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[SEARCH_SERVICE_TYPE] = data
        }
    }

    suspend fun saveChatDefaultModeTypeData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[CHAT_DEFAULT_MODEL_TYPE] = data
        }
    }

    suspend fun saveBuildTitleModeTypeData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[BUILD_TITLE_MODEL_TYPE] = data
        }
    }

    suspend fun saveBuildTitleTimeData(data: String) {
        context.dataStore.edit { preferences ->
            preferences[BUILD_TITLE_TIME] = data
        }
    }

    suspend fun saveTemperatureValue(data: Double) {
        context.dataStore.edit { preferences ->
            preferences[TEMPERATURE_VALUE] = data
        }
    }

    suspend fun saveUseTracelessSwitch(data: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_TRACELESS_SWITCH] = data
        }
    }

    // 从 DataStore 读取数据
    val readData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[API_KEY]
        }

    val readChatListNumber: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[CHAT_LIST_NUMBER]
        }

    val readChatIs: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[IS_CHAT]
        }

    val readServiceUrl: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[SERVICE_URL]
        }

    val readModelType: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[MODEL_TYPE]
        }

    val readLastModelType: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_MODEL_TYPE]
        }

    val readLastPosition: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_SELECTED_POSITION]
        }

    val readImageUrl: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[IMAGE_URL]
        }

    val readCueWords: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[CUE_WORDS]
        }

    val readCueWordsSwitch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[CUE_WORDS_SWITCH]
        }

    val readOfficialWordsSwitch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[OFFICIAL_WORDS_SWITCH]
        }

    val readClearWordsSwitch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[CLEAR_WORDS_SWITCH]
        }

    val readPreSwitch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[PRE_SWITCH]
        }

    val readExtractSwitch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[EXTRACT_SWITCH]
        }

    val readSearchSwitch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[SEARCH_SWITCH]
        }

    val readAppEmojisData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[APP_EMOJIS]
        }

    val readCustomizeKeyData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[CUSTOMIZE_API_KEY]
        }

    val readCustomizeServiceUrlData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[CUSTOMIZE_SERVICE_URL]
        }

    val readCustomizeModelIdData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[CUSTOMIZE_MODEL_ID]
        }

    val readServiceProviderData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[SERVICE_PROVIDER]
        }

    val readIsChangeModelSetting: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[IS_CHANGE_MODEL_SETTING]
        }

    val readOpenAiKeyData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[OPEN_AI_API_KEY]
        }

    val readAnthropicKeyData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[ANTHROPIC_API_KEY]
        }

    val readUserNameData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_NAME]
        }

    val readUserBalanceData: Flow<Double?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_BALANCE]
        }

    val readUserEmailData: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_EMAIL]
        }

    val readTemporaryModelType: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TEMPORARY_MODEL_TYPE]
        }

    val readTemporaryChatTitle: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TEMPORARY_CHAT_TITLE]
        }

    val readBuildTitleSwitch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[BUILD_TITLE_SWITCH]
        }

    val readSlideBottomSwitch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[SLIDE_BOTTOM_SWITCH]
        }

    val readSearchServiceType: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[SEARCH_SERVICE_TYPE]
        }

    val readChatDefaultModelType: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[CHAT_DEFAULT_MODEL_TYPE]
        }

    val readBuildTitleModelType: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[BUILD_TITLE_MODEL_TYPE]
        }

    val readBuildTitleTime: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[BUILD_TITLE_TIME]
        }

    val readTemperatureValue: Flow<Double?> = context.dataStore.data
        .map { preferences ->
            preferences[TEMPERATURE_VALUE]
        }

    val readUseTracelessSwitch: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[USE_TRACELESS_SWITCH]
        }


}