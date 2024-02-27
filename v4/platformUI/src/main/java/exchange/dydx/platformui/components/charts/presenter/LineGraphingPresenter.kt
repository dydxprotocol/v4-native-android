package exchange.dydx.platformui.components.charts.presenter

class LineGraphingPresenter(
    val config: GraphingPresenterConfig<LineChartData>,
    val doubleTapToZoom: Boolean = false,
    val drawFilled: Boolean = false,
) : GraphingPresenter<LineChartData>(config)

fun LineChartView.config() {
}
