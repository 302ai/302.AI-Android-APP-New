package com.newAi302.app.widget.dialog.base

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   : 中间显示弹窗
 * version: 1.0
 */
abstract class BaseCenterDialog(context: Context) : BaseDialog(context) {

    private var view: View? = null
    private var isAnimation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.decorView?.setPadding(0, 0, 0, 0)
        val layoutParams = window?.attributes
        layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams?.width = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams?.gravity = Gravity.CENTER
        window?.setAttributes(layoutParams)
        if (getView() == null) {
            return
        }
        view = getView()
        view?.let { setContentView(it) }
        initView(view)
//        upAnimator()
    }

    abstract fun getView(): View?

    abstract fun initView(view: View?)

    fun setAnimation(animation: Boolean) {
        isAnimation = animation
    }

    /**
     * 启动动画
     */
    protected fun upAnimator() {
        if (view != null && isAnimation) {
            val set = AnimationSet(true)
            set.addAnimation(AlphaAnimation(0f, 1f))
            set.addAnimation(
                ScaleAnimation(
                    0f,
                    1f,
                    0f,
                    1f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )
            )
            set.interpolator = AccelerateInterpolator()
            set.setDuration(200)
            set.setFillAfter(true)
            view?.setAnimation(set)
        }
    }

    /**
     * 关闭弹窗的动画
     */
    protected fun downAnimator() {
        if (isAnimation) {
            val set = AnimationSet(true)
            set.addAnimation(AlphaAnimation(1f, 0f))
            set.addAnimation(
                ScaleAnimation(
                    1f,
                    0f,
                    1f,
                    0f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )
            )
            set.interpolator = AccelerateInterpolator()
            set.setDuration(200)
            set.setFillAfter(true)
            set.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    dismiss()
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            view?.setAnimation(set)
            view?.let { setContentView(it) }
        } else {
            super.cancel()
        }
    }

    override fun cancel() {
        super.cancel()
        downAnimator()
    }
}