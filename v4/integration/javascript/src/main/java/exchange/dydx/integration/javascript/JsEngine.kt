package exchange.dydx.integration.javascript

interface JsEngine {
    suspend fun runJs(function: String, params: List<String>, callback: ResultCallback)
    suspend fun runJs(script: String, callback: ResultCallback)
}
