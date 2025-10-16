package com.newAi302.app.utils

import android.graphics.drawable.Drawable
import android.text.Spanned
import android.text.style.ImageSpan
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.newAi302.app.infa.OnMarkdownImageClickListener
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.AsyncDrawableScheduler
import java.lang.ref.WeakReference

// 自定义图片加载器，用于监听图片点击
class ImageClickPlugin(
    private val listener: WeakReference<OnMarkdownImageClickListener>
) : AbstractMarkwonPlugin() {

    override fun afterSetText(textView: TextView) {
        super.afterSetText(textView)
        // 1. 先验证 text 是否为 Spanned，若不是则尝试重新获取
        val initialText = textView.text
        if (initialText !is Spanned) {
            Log.e("ImageClickPlugin", "text 不是 Spanned 类型，尝试重新渲染")
            // 若类型丢失，重新触发 Markwon 渲染（确保生成 Spanned）
            (textView.tag as? Markwon)?.let { markwon ->
                markwon.setMarkdown(textView, initialText.toString())
            }
            return
        }

        // 2. 延迟检查：等待图片异步加载完成（根据需求调整延迟时间，通常 300-800ms）
        textView.postDelayed({
            checkImageSpans(textView)
        }, 500) // 关键：给 Glide 留出图片加载时间

        // 3. 额外保险：监听图片加载完成事件（彻底解决异步时序问题）
        listenToImageLoadComplete(textView, initialText as Spanned)
    }

    /**
     * 检查 text 中的 ImageSpan，绑定点击事件
     */
    private fun checkImageSpans(textView: TextView) {
        val text = textView.text as? Spanned ?: run {
            Log.e("ImageClickPlugin", "text 仍不是 Spanned，无法获取图片")
            return
        }

        // 遍历所有 ImageSpan（Markwon 解析图片后生成的 Span）
        val imageSpans = text.getSpans(0, text.length, ImageSpan::class.java)
        if (imageSpans.isEmpty()) {
            Log.e("ImageClickPlugin", "未找到 ImageSpan，可能图片未解析/加载失败")
            // 尝试再次延迟检查（应对慢网络场景）
            textView.postDelayed({ checkImageSpans(textView) }, 300)
            return
        }

        // 为每个图片绑定点击事件
        for (imageSpan in imageSpans) {
            val asyncDrawable = imageSpan.drawable as? AsyncDrawable ?: continue
            val imageView = getAsyncDrawableView(asyncDrawable) ?: continue

            // 避免重复绑定点击事件
            if (imageView.tag != null && imageView.tag as Boolean) return
            imageView.tag = true // 标记已绑定

            imageView.setOnClickListener {
                val imageUrl = asyncDrawable.destination.toString()
                listener.get()?.onImageClick(imageUrl)
            }
        }
    }

    /**
     * 监听 AsyncDrawable 加载完成事件（彻底解决异步问题）
     */
    private fun listenToImageLoadComplete(textView: TextView, spanned: Spanned) {
        val imageSpans = spanned.getSpans(0, spanned.length, ImageSpan::class.java)
        for (imageSpan in imageSpans) {
            val asyncDrawable = imageSpan.drawable as? AsyncDrawable ?: continue
            // 监听 Drawable 加载状态（通过 Drawable.Callback）
            asyncDrawable.callback = object : Drawable.Callback {
                override fun invalidateDrawable(who: Drawable) {
                    textView.invalidate()
                }

                override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
                    textView.scheduleDrawable(who, what, `when`)
                }

                override fun unscheduleDrawable(who: Drawable, what: Runnable) {
                    textView.unscheduleDrawable(who, what)
                }
            }

            // 检查图片是否已加载完成（若已加载则直接处理）
            if (asyncDrawable.result != null) {
                checkImageSpans(textView)
            } else {
                // 图片未加载完成：注册加载回调（需 Markwon 1.10.0+）
                AsyncDrawableScheduler.schedule(textView)
            }
        }
    }

    /**
     * 反射获取 AsyncDrawable 中的 ImageView（兼容私有字段）
     */
    private fun getAsyncDrawableView(drawable: AsyncDrawable): ImageView? {
        return try {
            val field = AsyncDrawable::class.java.getDeclaredField("view")
            field.isAccessible = true
            field.get(drawable) as? ImageView
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 图片点击回调接口
    interface OnMarkdownImageClickListener {
        fun onImageClick(imageUrl: String)
    }
}