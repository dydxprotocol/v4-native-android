package exchange.dydx.platformui.components.charts.touch

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.view.MotionEvent
import android.view.View
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.listener.BarLineChartTouchListener

class LongPressTouchListener(
    chart: BarLineChartBase<out BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry?>?>?>?,
    touchMatrix: Matrix?,
    dragTriggerDistance: Float
) : BarLineChartTouchListener(chart, touchMatrix, dragTriggerDistance) {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        var handled = false
        val touch =
            // Handle touch events here...
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_MOVE -> {
                    if (mTouchMode == NONE && mChart.isHighlightPerDragEnabled) {
                        mLastGesture = ChartGesture.DRAG
                        if (mChart.isHighlightPerDragEnabled) {
                            performHighlightDrag(event)
                            handled = true
                        } else {}
                    } else {
                    }
                }

                else -> {}
            }
        return if (handled) {
            true
        } else {
            super.onTouch(v, event)
        }
    }

    /**
     * Highlights upon dragging, generates callbacks for the selection-listener.
     *
     * @param e
     */
    private fun performHighlightDrag(e: MotionEvent) {
        val h = mChart.getHighlightByTouchPoint(e.x, e.y)
        if (h != null && !h.equalTo(mLastHighlighted)) {
            mLastHighlighted = h
            mChart.highlightValue(h, true)
        }
    }
}
