package exchange.dydx.utilities.utils

interface WorkerProtocol {
    fun start()
    fun stop()
    var isStarted: Boolean
}
