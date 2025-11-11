import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ScrollView
import androidx.recyclerview.widget.RecyclerView
//import timber.log.Timber // 若未集成Timber，可替换为Log.d（需添加android.util.Log依赖）

/**
 * 修复版：测量View Y坐标工具类（解决0坐标问题）
 */
object ViewYCoordinateHelper {

    // 调试标签（方便排查问题）
    private const val TAG = "ViewYCoordinateHelper"

    /**
     * 安全将Context转为Activity
     */
    private fun Context.toActivity(): Activity? {
        return when (this) {
            is Activity -> this
            else -> {
                Log.e("ceshi","Context不是Activity类型，测量失败")
                null
            }
        }
    }

    /**
     * 测量 View 相对于父布局的 Y 坐标（含translationY + 父布局滚动偏移）
     * @param includeScroll  是否包含父布局的滚动偏移（默认true，解决滚动容器中坐标为0的问题）
     */
    fun getRelativeParentY(
        activityContext: Context,
        targetView: View,
        includeScroll: Boolean = true,
        callback: (Float) -> Unit
    ) {
        val activity = activityContext.toActivity() ?: return
        checkViewValid(targetView) ?: return

        measureAfterLayout(targetView) {
            // 基础Y坐标（含translationY）
            var y = targetView.y
            // 若需要包含父布局滚动偏移，递归计算所有可滚动父布局的scrollY
            if (includeScroll) {
                y += getParentScrollY(targetView.parent as? View)
            }
            Log.e("ceshi","相对父布局Y：$y px（含滚动偏移：$includeScroll）")
            callback(y)
        }
    }

    /**
     * 测量 View 相对于屏幕的绝对 Y 坐标（含状态栏/导航栏，不受滚动影响）
     * 🔥 推荐优先使用这个方法（最稳定，不受父布局滚动/嵌套影响）
     */
    fun getAbsoluteScreenY(
        activityContext: Context,
        targetView: View,
        callback: (Int) -> Unit
    ) {
        val activity = activityContext.toActivity() ?: return
        checkViewValid(targetView) ?: return

        measureAfterLayout(targetView) {
            val location = IntArray(2)
            targetView.getLocationOnScreen(location) // 不受滚动影响，绝对坐标
            val screenY = location[1]
            Log.e("ceshi","屏幕绝对Y：$screenY px（含状态栏）")
            callback(screenY)
        }
    }

    /**
     * 测量 View 相对于 Activity 窗口的 Y 坐标（不含状态栏，含标题栏）
     */
    fun getRelativeWindowY(
        activityContext: Context,
        targetView: View,
        callback: (Int) -> Unit
    ) {
        val activity = activityContext.toActivity() ?: return
        checkViewValid(targetView) ?: return

        measureAfterLayout(targetView) {
            val location = IntArray(2)
            targetView.getLocationInWindow(location)
            val windowY = location[1]
            Log.e("ceshi","窗口内Y：$windowY px（不含状态栏）")
            callback(windowY)
        }
    }

    /**
     * 关键修复1：检查View是否具备测量条件（避免无效测量）
     * @return null 表示无效，非null表示有效
     */
    private fun checkViewValid(view: View): View? {
        return when {
            !view.isAttachedToWindow -> {
                Log.e("ceshi","View未附加到窗口（isAttachedToWindow = false），无法测量")
                null
            }
            view.visibility != View.VISIBLE -> {
                Log.e("ceshi","View不可见（visibility = ${view.visibility}），测量结果可能为0")
                null // 若需支持INVISIBLE，可改为 return view，但需提醒用户
            }
            view.layoutParams == null -> {
                Log.e("ceshi","View未设置布局参数（layoutParams = null），无法测量")
                null
            }
            else -> view
        }
    }

    /**
     * 关键修复2：优化测量时机（确保布局+绘制前稳定）
     * 改用 OnPreDrawListener（比post()更晚，布局完全稳定后触发）
     */
    private fun measureAfterLayout(view: View, action: () -> Unit) {
        // 已布局完成，直接执行
        if (view.width > 0 && view.height > 0) {
            action()
            return
        }

        // 方案：OnPreDrawListener（绘制前触发，布局已完全确定，且自动移除）
        val preDrawListener = object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                // 移除监听（避免多次触发）
                view.viewTreeObserver.removeOnPreDrawListener(this)
                // 再次检查View有效性（防止测量过程中View被移除）
                if (checkViewValid(view) != null) {
                    action()
                }
                return true // 返回true，不影响View绘制
            }
        }

        // 添加监听（若View已销毁，避免空指针）
        if (view.viewTreeObserver.isAlive) {
            view.viewTreeObserver.addOnPreDrawListener(preDrawListener)
        } else {
            // 兜底：若ViewTreeObserver已销毁，用postDelayed延迟一点时间
            view.postDelayed({
                if (checkViewValid(view) != null) {
                    action()
                }
            }, 100) // 100ms足够布局完成（避免过度延迟）
        }
    }

    /**
     * 关键修复3：计算所有父布局的滚动偏移（解决ScrollView/RecyclerView中坐标为0的问题）
     * 递归遍历父View，累加可滚动容器的scrollY
     */
    private fun getParentScrollY(parentView: View?): Float {
        var scrollY = 0f
        var parent = parentView
        while (parent != null) {
            // 支持ScrollView、RecyclerView、NestedScrollView等可滚动容器
            when (parent) {
                is ScrollView -> scrollY += parent.scrollY
                is RecyclerView -> scrollY += parent.scrollY
                else -> {
                    // 其他可滚动View（如HorizontalScrollView不影响Y轴，忽略）
                    scrollY += parent.scrollY
                }
            }
            // 继续遍历上一级父View
            parent = parent.parent as? View
        }
        return scrollY
    }
}