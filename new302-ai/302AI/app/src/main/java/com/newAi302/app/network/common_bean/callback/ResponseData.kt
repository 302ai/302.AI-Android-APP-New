package com.newAi302.app.network.common_bean.callback

import com.newAi302.app.bean.LoginBean

data class ResponseData(
    val code: Int,
    val msg: String,
    val data: LoginBean
)

data class ResponseAliData(
    val code: Int,
    val msg: String,
    val data: AliPayData
)

data class ResponseUsdtData(
    val code: Int,
    val msg: String,
    val data: UsdtPayData
)

data class ResponseStripeData(
    val code: Int,
    val msg: String,
    val data: StripePayData
)

data class TokenData(
    val token: String
)

data class AliPayData(
    val key: String,
    val to: String,
    val type: Int
)

data class UsdtPayData(
    val key: String,
    val url: String,
    val type: Int
)

data class StripePayData(
    val session_id: String,
    val jk: String,
    val hk: String,
    val pk: String,
    val a: String
)