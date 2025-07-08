package exchange.dydx.platformui.components.charts.touch

import android.view.MotionEvent
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener

class LineChartTouchHandler(
    private val chart: BarLineChartBase<BarLineScatterCandleBubbleData<IBarLineScatterCandleBubbleDataSet<Entry>>>,
) : OnChartGestureListener {
    override fun onChartGestureStart(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        chart.isHighlightPerTapEnabled = false
        chart.isHighlightPerDragEnabled = false
    }

    override fun onChartGestureEnd(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        chart.isHighlightPerTapEnabled = false
        chart.isHighlightPerDragEnabled = false
        chart.onTouchListener.setLastHighlighted(null);
        chart.isSelected = false
        chart.highlightValue(null, true);
    }

    override fun onChartLongPressed(me: MotionEvent?) {
        chart.isHighlightPerDragEnabled = true
    }

    override fun onChartDoubleTapped(me: MotionEvent?) {
    }

    override fun onChartSingleTapped(me: MotionEvent?) {
        chart.isHighlightPerTapEnabled = false
        chart.isHighlightPerDragEnabled = false
        chart.onTouchListener.setLastHighlighted(null);
        chart.highlightValue(null, true);
    }

    override fun onChartFling(
        me1: MotionEvent?,
        me2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ) {
    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
    }
}
