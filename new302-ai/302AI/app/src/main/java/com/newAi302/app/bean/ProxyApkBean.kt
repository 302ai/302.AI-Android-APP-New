package com.newAi302.app.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/*{
    "code": 0,
    "msg": "success",
    "data": {
    "value": "g1wqp68zfkohjpb8q990aezmjp5kq4ka",
    "domain": "proxy.runfordec.autos",
    "port": 4100
 }
}*/

@Parcelize
data class ProxyApkBean(
    val min_version: String = "",
    val recommend_version: String = "",
    val latest_download_url: String = ""
) : Parcelable