package com.newAi302.app.data

/**
 * 封装 View 在屏幕上的绝对位置（X、Y 坐标）
 * @param x 屏幕水平坐标（从屏幕左侧边缘开始计算）
 * @param y 屏幕垂直坐标（从屏幕顶部边缘开始计算，含状态栏）
 */
data class ScreenPosition(val x: Int, val y: Int)