package com.newAi302.app.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/15
 * desc   : html data
 * version: 1.0
 */
//连接
@Parcelize
data class HtmlProxyJsonBean(
    val proxy__country__name: String = "",
    val proxy__country__code: String = "",
    val proxy__state__name: String = "",
    val proxy__city__name: String = "",
    val created_on: Long = 0,
    val traffic_usage: String = "",
    val cost: Double = 0.0,
    val proxy__health: Long = 0,
    val proxy__status: Int = 0,
    val expired: Boolean = false,
    val life_time: Long = 0,
    val network: String = "",
    val data_center: Int = 0,
    val url: String = "",
    val username: String = "",
    val passwd: String = "",
    val remark: String = "",
    val proxy__state_id: Int = 0,
    val proxy__country_id: Int = 0,
    val proxy__city_id: String = "",
    val proxy__online: Int = 0,
    val is_deleted: Boolean = false,
    val ip_addr: String = "",
    val proxy_source: String = "",
    val expired_on: Long = 0,
    val port: Int = 0,
    val req_proxy_url: String = "",
    val ads_proxy_url: String = "",
    val enable: Boolean = false,
    val status: Int = 0,
    val proxy_type: String = "",
    val proxy_type_zh: String = "",
    val proxy_type_en: String = "",
    val proxy_type_ru: String = "",
    val proxy_type_jp: String = "",
    val proxy_area: String = "",
    val generate_type: String = "",
    val statusLoading: Boolean = false,
    val renewalLoading: Boolean = false,
    val rotationLoading: Boolean = false
) : Parcelable

//去充值
@Parcelize
data class HtmlChargeJsonBean(
    val id: Int = 0,
    val payway: String = "",
    val note: String = "",
    val en_note: String = "",
    val price: Long = 0,
    val service_fee: Double = 0.0,
    val extra_value: Int = 0,
    val tag: String = "",
    val en_tag: String = "",
    val isSelect: Boolean = false
) : Parcelable

//快速访问
@Parcelize
data class HtmlQuickAccessBean(
    val username: String = "",
    val remark: String = "",
    val proxy_type: String = "",
    val proxy_type_zh: String = "",
    val proxy_type_en: String = "",
    val proxy_type_jp: String = "",
    val proxy_type_ru: String = "",
    val ads_url: String = "",
    val token_id: Long = 0,
    val uid: Long = 0,
    val proxy_area: String = "",
    val created_on: Long = 0,
    val ip: String = "",
    val modified_on: Long = 0,
    val url: String = "",
    val proxy_created_on: Long = 0,
    val id: Int = 0,
    val group: String = "",
    val deleted_on: Int = 0,
    val proxy_remark: String = "",
    val network: String = ""
) : Parcelable