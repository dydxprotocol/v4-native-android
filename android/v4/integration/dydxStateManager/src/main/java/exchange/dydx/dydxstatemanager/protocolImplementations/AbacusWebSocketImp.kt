package exchange.dydx.dydxstatemanager.protocolImplementations

import exchange.dydx.utilities.utils.Logging
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbacusWebSocketImp @Inject constructor(
    private val logger: Logging,
) : exchange.dydx.abacus.protocols.WebSocketProtocol {

    private val TAG = "AbacusWebSocketImp"

    private var url: String? = null
    private var connected: ((Boolean) -> Unit)? = null
    private var received: ((String) -> Unit)? = null

    private var webSocket: WebSocket? = null

    private val NORMAL_CLOSURE = 1000

    override fun connect(
        url: String,
        connected: (result: Boolean) -> Unit,
        received: (message: String) -> Unit,
    ) {
        this.url = url
        this.connected = connected
        this.received = received
        connect()
    }

    override fun disconnect() {
        webSocket?.close(NORMAL_CLOSURE, null)
        webSocket = null
    }

    override fun send(message: String) {
        webSocket?.send(message)
    }

    private fun connect() {
        url?.let { url ->
            val request: Request?

            try {
                request = Request.Builder().url(url).build()
            } catch (e: Exception) {
                connected?.invoke(false)
                logger.e(TAG, "AbacusWebSocketImp Invalid URL $url, ${e.message}")
                return
            }

            if (request != null) {
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build()

                val listener = object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        this@AbacusWebSocketImp.webSocket = webSocket
                        connected?.invoke(true)
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        received?.invoke(text)
                    }

                    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                        // Handle binary messages if needed
                    }

                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                        logger.d(TAG, "AbacusWebSocketImp Closing $code $reason")
                        webSocket.close(NORMAL_CLOSURE, null)
                        connected?.invoke(false)
                    }

                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: Response?
                    ) {
                        logger.e(TAG, "AbacusWebSocketImp Error ${t.message}")
                        connected?.invoke(false)
                    }
                }

                okHttpClient.newWebSocket(request, listener)
            }
        }
    }
}
