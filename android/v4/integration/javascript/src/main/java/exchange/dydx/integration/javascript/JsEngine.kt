package exchange.dydx.integration.javascript

interface JsEngine {
    fun runJs(function: String, params: List<String>, callback: ResultCallback)
    fun runJs(script: String, callback: ResultCallback)
}
