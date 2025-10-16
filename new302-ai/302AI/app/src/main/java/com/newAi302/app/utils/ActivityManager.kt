package com.newAi302.app.utils

object ActivityManager {
    // 存储所有活动的Activity
    private val activityStack = mutableListOf<android.app.Activity>()

    // 添加Activity到栈
    fun addActivity(activity: android.app.Activity) {
        activityStack.add(activity)
    }

    // 从栈中移除Activity
    fun removeActivity(activity: android.app.Activity) {
        activityStack.remove(activity)
    }

    // 清除除了指定Activity之外的所有Activity
    fun finishAllExcept(clazz: Class<*>) {
        val iterator = activityStack.iterator()
        while (iterator.hasNext()) {
            val activity = iterator.next()
            if (activity.javaClass != clazz) {
                iterator.remove() // 先从栈中移除
                activity.finish() // 再关闭Activity
            }
        }
    }

    // 新增：保留指定实例，关闭所有其他Activity（包括同类型的其他实例）
    fun finishAllExcept(keepActivity: android.app.Activity) {
        val iterator = activityStack.iterator()
        while (iterator.hasNext()) {
            val activity = iterator.next()
            if (activity !== keepActivity) { // 注意用!==判断实例是否不同
                iterator.remove()
                activity.finish()
            }
        }
    }

    // 清除所有Activity
    fun finishAll() {
        activityStack.forEach { it.finish() }
        activityStack.clear()
    }
}