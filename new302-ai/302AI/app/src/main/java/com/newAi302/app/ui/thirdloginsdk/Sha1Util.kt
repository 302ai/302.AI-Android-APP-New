package com.newAi302.app.ui.thirdloginsdk

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Base64
import com.newAi302.app.utils.LogUtils
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Locale

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/16
 * desc   :
 * version: 1.0
 */

/**
 * 用来获取 facebook 的 keyHash 和 google 的 Sha1 key。
 */
object Sha1Util {

    fun getKeyHash(context: Context): String {
        LogUtils.i("Sha1Util", "sha1 = \$sha1")
        var keyHash = ""
        try {
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures!!) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                LogUtils.d("Sha1Util", "KeyHash: $keyHash")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return keyHash
    }

    @Throws(
        PackageManager.NameNotFoundException::class,
        CertificateException::class,
        NoSuchAlgorithmException::class
    )
    fun getCertificateSHA1Fingerprint(context: Context): String {
        val pm = context.packageManager
        val pName = context.packageName
        val flags = PackageManager.GET_SIGNATURES
        var pInfo: PackageInfo? = null

        pInfo = pm.getPackageInfo(pName, flags)
        val cert = pInfo.signatures!![0]?.toByteArray()
        val input: InputStream = ByteArrayInputStream(cert)
        val cf = CertificateFactory.getInstance("X509")
        val c = cf.generateCertificate(input) as X509Certificate
        val md = MessageDigest.getInstance("SHA1")
        val publicKey = md.digest(c.encoded)
        return byte2HexFormatted(publicKey)
    }

    private fun byte2HexFormatted(arr: ByteArray): String {
        val str = StringBuilder(arr.size * 2)
        for (i in arr.indices) {
            var h = Integer.toHexString(arr[i].toInt())
            val l = h.length
            if (l == 1) {
                h = "0$h"
            }
            if (l > 2) {
                h = h.substring(l - 2, l)
            }
            str.append(h.uppercase(Locale.getDefault()))
            if (i < (arr.size - 1)) {
                str.append(':')
            }
        }
        return str.toString()
    }
}