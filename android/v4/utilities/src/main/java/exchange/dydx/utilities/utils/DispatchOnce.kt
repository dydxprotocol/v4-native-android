package exchange.dydx.utilities.utils

object DispatchOnce {
    @Synchronized
    fun run(token: Token, runnable: Runnable) {
        if (!token.hasRun) {
            token.hasRun = true
            runnable.run()
        }
    }

    class Token {
        var hasRun = false
    }
}
