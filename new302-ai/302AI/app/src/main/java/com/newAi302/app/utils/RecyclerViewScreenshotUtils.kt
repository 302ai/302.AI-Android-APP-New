package com.newAi302.app.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newAi302.app.adapter.MyAdapter

object RecyclerViewScreenshotUtils {

    /**
     * 对RecyclerView进行长截屏（包含所有item）
     * @param recyclerView 目标RecyclerView
     * @return 长截屏 bitmap（null表示失败）
     */
    fun captureFullRecyclerView(recyclerView: RecyclerView): Bitmap? {
        val adapter = recyclerView.adapter ?: return null
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return null

        // 1. 保存当前RecyclerView的滚动位置（用于后续恢复）
        val originalPosition = layoutManager.findFirstVisibleItemPosition()
        val originalTop = if (originalPosition != RecyclerView.NO_POSITION) {
            layoutManager.findViewByPosition(originalPosition)?.top ?: 0
        } else 0

        // 2. 计算RecyclerView总高度（所有item高度+间距）
        val totalHeight = calculateTotalHeight(recyclerView, adapter, layoutManager)
        if (totalHeight <= 0) return null

        // 3. 创建与RecyclerView等宽、总高度的bitmap
        val width = recyclerView.width
        val screenshotBitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(screenshotBitmap)
        recyclerView.background?.draw(canvas) // 绘制RecyclerView背景

        // 4. 滚动到顶部，逐部分绘制所有item
        layoutManager.scrollToPosition(0)
        recyclerView.invalidate() // 强制刷新视图

        var currentHeight = 0 // 记录当前绘制到的高度
        val itemCount = adapter.itemCount

        while (currentHeight < totalHeight && layoutManager.findFirstVisibleItemPosition() < itemCount) {
            // 获取当前可见的第一个item
            val firstVisibleView = layoutManager.findViewByPosition(layoutManager.findFirstVisibleItemPosition())
                ?: break

            // 绘制当前可见区域到画布
            val bitmap = getViewBitmap(firstVisibleView)
            canvas.drawBitmap(bitmap, 0f, currentHeight.toFloat(), null)
            bitmap.recycle() // 及时回收临时bitmap

            // 计算当前可见区域高度，累加绘制高度
            currentHeight += firstVisibleView.height

            // 滚动到下一个可见区域（每次滚动一个item高度）
            recyclerView.scrollBy(0, firstVisibleView.height)
        }

        // 5. 恢复RecyclerView原始滚动位置
        layoutManager.scrollToPositionWithOffset(originalPosition, originalTop)

        return screenshotBitmap
    }

    /**
     * 计算RecyclerView所有item的总高度（包含间距）
     */
    private fun calculateTotalHeight(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        layoutManager: LinearLayoutManager
    ): Int {
        var totalHeight = 0
        val itemCount = adapter.itemCount


        val myAdapter = adapter as? MyAdapter
        myAdapter?.let { adapterRef ->
            for (i in 0 until itemCount) {
                val viewType = adapterRef.getItemViewType(i)
                // 创建具体的 MyViewHolder
                val viewHolder = adapterRef.createViewHolder(recyclerView, viewType) as MyAdapter.MyViewHolder
                // 现在就能正确调用 onBindViewHolder 了，因为类型匹配
                adapterRef.onBindViewHolder(viewHolder, i)

                // 后续测量等逻辑...
                val widthSpec = View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY)
                val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                viewHolder.itemView.measure(widthSpec, heightSpec)

                // 累加item高度
                totalHeight += viewHolder.itemView.measuredHeight

                // 累加ItemDecoration的垂直间距（顶部+底部）
                val decorCount = recyclerView.itemDecorationCount
                for (d in 0 until decorCount) {
                    val outRect = android.graphics.Rect()
                    recyclerView.getItemDecorationAt(d).getItemOffsets(
                        outRect,
                        viewHolder.itemView,
                        recyclerView,
                        RecyclerView.State()
                    )
                    totalHeight += outRect.top + outRect.bottom
                }
            }
        }
        // 遍历所有item，累加高度（包含ItemDecoration间距）
        /*for (i in 0 until itemCount) {
            // 获取item视图（通过Adapter创建临时ViewHolder）
            val viewType = adapter.getItemViewType(i)
            val viewHolder = adapter.createViewHolder(recyclerView, viewType)
            adapter.onBindViewHolder(viewHolder,i)

            // 测量item尺寸（宽度与RecyclerView一致）
            val widthSpec = View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            viewHolder.itemView.measure(widthSpec, heightSpec)

            // 累加item高度
            totalHeight += viewHolder.itemView.measuredHeight

            // 累加ItemDecoration的垂直间距（顶部+底部）
            val decorCount = recyclerView.itemDecorationCount
            for (d in 0 until decorCount) {
                val outRect = android.graphics.Rect()
                recyclerView.getItemDecorationAt(d).getItemOffsets(
                    outRect,
                    viewHolder.itemView,
                    recyclerView,
                    RecyclerView.State()
                )
                totalHeight += outRect.top + outRect.bottom
            }
        }*/

        // 加上RecyclerView自身的padding
        totalHeight += recyclerView.paddingTop + recyclerView.paddingBottom
        return totalHeight
    }

    /**
     * 将单个View转换为Bitmap
     */
    private fun getViewBitmap(view: View): Bitmap {
        // 测量View尺寸
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY)
        )
        // 布局View（确保位置正确）
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        // 绘制View到Bitmap
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}