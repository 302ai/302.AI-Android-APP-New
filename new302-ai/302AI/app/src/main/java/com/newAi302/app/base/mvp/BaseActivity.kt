package com.newAi302.app.base.mvp

import android.app.Activity
import android.app.Dialog
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.viewbinding.ViewBinding
import com.newAi302.app.utils.StatusBarUtil
import com.newAi302.app.utils.ToastUtils
import com.newAi302.app.widget.dialog.base.DialogFactory
import com.google.android.material.snackbar.Snackbar
import com.newAi302.app.utils.ActivityManager
import com.newAi302.app.utils.LanguageUtil
import com.newAi302.app.utils.ThemeUtil


/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   : BaseActivity 基础类
 * version: 1.0
 */
abstract class BaseActivity<B : ViewBinding?> : AppCompatActivity(), BaseIView {

    private var uiMode = 0
    var inAnimType: String? = null
    protected var mBinding: B? = null
    private var loadingDialog: Dialog? = null
    private var mStartTime: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        val customizeTheme = ThemeUtil.getSavedTheme(this)
        // 初始化SharedPreferences
        //sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        Log.e("ceshi","获取的主题是:$customizeTheme")
        if (customizeTheme == ThemeUtil.THEME_LIGHT){
            ThemeUtil.changeTheme(this, ThemeUtil.THEME_LIGHT)
        }else if (customizeTheme == ThemeUtil.THEME_NIGHT){
            ThemeUtil.changeTheme(this, ThemeUtil.THEME_NIGHT)
        }else{
            ThemeUtil.changeTheme(this, ThemeUtil.THEME_FOLLOW_SYSTEM)
        }
        // 在 onCreate 前应用语言设置
        applyLanguage()
        // 将当前Activity添加到管理器
        ActivityManager.addActivity(this)
        super.onCreate(savedInstanceState)
        mStartTime = System.currentTimeMillis()
        mBinding = createDataBinding()
        if (mBinding != null) {
            this.setContentView(mBinding?.root)
        }
        if (this !is MVPBaseActivity<*, *, *>) {
            initView()
            initListener()
            initData(savedInstanceState)
        }
        settingBarColor()
    }

    abstract fun createDataBinding(): B
    abstract fun initView()
    abstract fun initListener()
    override fun initData(savedInstanceState: Bundle?) {}
    override fun setContentView(view: View?) {
        super.setContentView(view)
    }

    fun settingBarColor() {
        StatusBarUtil.setRootViewFitsSystemWindows(this, false)
        StatusBarUtil.setTranslucentStatus(this)
        settingBarColor(this)
    }

    private fun settingBarColor(activity: Activity) {
        /*  val mode: Int = WearData.getInstance().getAppSession().getThemeMode()
          if (mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
              if (WearUtil.isSystemNightMode(this)) {
                  StatusBarUtil.setStatusBarDarkTheme(activity, false)
              } else {
                  StatusBarUtil.setStatusBarDarkTheme(activity, true)
              }
          } else if (mode == AppCompatDelegate.MODE_NIGHT_YES) { //黑板
              StatusBarUtil.setStatusBarDarkTheme(activity, false)
          } else if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
              StatusBarUtil.setStatusBarDarkTheme(activity, true)
          }*/
        StatusBarUtil.setStatusBarDarkTheme(activity, true)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && mStartTime > 0) {
            val focusTime = System.currentTimeMillis()
            val costTime = focusTime - mStartTime
            mStartTime = 0
        }
    }

    override fun showLoading() {
        if (isFinishing || isDestroyed) {
            return
        }
        if (loadingDialog == null) {
            loadingDialog = DialogFactory.createLoadingDialog(this) {}
        }
        loadingDialog?.show()
    }

    override fun hideLoading() {
        if (isFinishing || isDestroyed) {
            return
        }
        if (loadingDialog != null) {
            loadingDialog?.dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
//        if (this !is MVPBaseActivity) {
//            if (this.isFinishing) {
//                onWillDestroy()
//            }
//        }
    }

    protected fun onWillDestroy() {}
    override fun onDestroy() {
        super.onDestroy()
//        ReportManager.log(this.javaClass.getSimpleName() + " onDestroy() ")
        // 将当前Activity从管理器中移除
        ActivityManager.removeActivity(this)
        if (loadingDialog != null) {
            loadingDialog!!.dismiss()
            loadingDialog = null
        }
    }

    override fun finish() {
        super.finish()
//        ARouterManager.startBackAnim(this, inAnimType)
    }

    override fun showError(errorMsg: String?) {
        hideLoading()
        ToastUtils.showShort(errorMsg)
    }

    override fun updateViews(type: Int, vararg data: Any?) {}

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        //隐藏系统键盘
        if (!isIgnoreKeyBoard()) {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                val v = currentFocus
                /*if (KeyboardUtils.isShouldHideInput(v, ev)) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v!!.windowToken, 0)
                }*/
                return super.dispatchTouchEvent(ev)
            }
        }
        var isWindowSuperDispatch = false
        try {
            // 必不可少，否则所有的组件都不会有TouchEvent了
            isWindowSuperDispatch = window.superDispatchTouchEvent(ev) || onTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isWindowSuperDispatch
    }

    protected fun isIgnoreKeyBoard(): Boolean {
        return false
    }

    override fun getResources(): Resources { //还原字体大小
        val res = super.getResources()
        val configuration = res.configuration
        if (configuration.fontScale != 1.0f) {
            configuration.fontScale = 1.0f
            res.updateConfiguration(configuration, res.displayMetrics)
        }
        return res
    }

    fun isBanSecure(): Boolean {
        return false

        // 需要屏蔽的页面如下，以后也需要用注释记录在下方做统一处理：
        // ChatActivity ChatGroupActivity ChatVoiceAndVideoActivity
    }

    companion object {
        /********防止按钮连续点击 */
        private var lastClickTime: Long = 0

        @get:Synchronized
        val isFastClick: Boolean
            get() {
                val time = System.currentTimeMillis()
                if (time - lastClickTime < 500) {
                    return true
                }
                lastClickTime = time
                return false
            }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 配置变化时重新应用语言设置
        applyLanguage()
        if (newConfig.uiMode != uiMode) {
            uiMode = newConfig.uiMode
            ActivityCompat.recreate(this)
        }
    }


    fun snackbar(@StringRes resId: Int): Snackbar = snackbar("").setText(resId)
    fun snackbar(text: CharSequence): Snackbar = snackbarInternal(text).apply {
        view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
            maxLines = 10
        }
    }

    internal open fun snackbarInternal(text: CharSequence): Snackbar = throw NotImplementedError()



    private fun applyLanguage() {
        val language = LanguageUtil.getSavedLanguage(this)
        val context = LanguageUtil.applyLanguage(this, language)
        val resources = context.resources
        val configuration = resources.configuration
        val displayMetrics = resources.displayMetrics
        // 更新配置
        resources.updateConfiguration(configuration, displayMetrics)
    }

    /**
     * 切换语言并重启Activity
     */
    protected fun switchLanguage(language: String) {
        LanguageUtil.saveLanguageSetting(this, language)
        // 重启当前Activity使语言设置生效
        recreate()
    }

}