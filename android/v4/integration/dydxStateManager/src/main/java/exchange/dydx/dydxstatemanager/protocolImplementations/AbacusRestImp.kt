package exchange.dydx.dydxstatemanager.protocolImplementations

import android.os.AsyncTask
import android.util.Log
import exchange.dydx.abacus.protocols.RestCallback
import exchange.dydx.abacus.protocols.RestProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.toJson
import exchange.dydx.utilities.utils.Logging
import okhttp3.CacheControl
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbacusRestImp @Inject constructor(
    private val logger: Logging,
) : RestProtocol {

    private val TAG = "AbacusRestImp"

    private var backgroundTaskId: Int = -1

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun delete(
        url: String,
        headers: IMap<String, String>?,
        callback: RestCallback,
    ) {
        processRest(url, headers, null, "DELETE", callback)
    }

    override fun get(
        url: String,
        headers: IMap<String, String>?,
        callback: RestCallback,
    ) {
        processRest(url, headers, null, "GET", callback)
    }

    override fun post(
        url: String,
        headers: IMap<String, String>?,
        body: String?,
        callback: RestCallback,
    ) {
        processRest(url, headers, body, "POST", callback)
    }

    override fun put(
        url: String,
        headers: IMap<String, String>?,
        body: String?,
        callback: RestCallback,
    ) {
        processRest(url, headers, body, "PUT", callback)
    }

    private fun processRest(
        url: String,
        headers: IMap<String, String>?,
        body: String?,
        verb: String,
        callback: RestCallback,
    ) {
        var requestBuilder = Request.Builder()

        try {
            requestBuilder.url(url)
        } catch (e: Exception) {
            logger.e(TAG, "AbacusRestImp Invalid URL $url, ${e.message}")
            callback(null, 0, null)
            return
        }

        val headers = headers?.toTypedArray()
        headers?.forEach { (key, value) ->
            requestBuilder = requestBuilder.header(key, value)
        }
        val requestBody = if (!body.isNullOrBlank()) {
            body.toRequestBody("application/json".toMediaTypeOrNull())
        } else {
            if (verb == "POST" || verb == "PUT") {
                // Those request types require a body
                "".toRequestBody()
            } else {
                null
            }
        }

        val request = requestBuilder
            .cacheControl(CacheControl.Builder().noStore().build())
            .method(verb, requestBody)
            .build()

        run(request, callback)
    }

    private fun run(request: Request, callback: RestCallback) {
        beginBackgroundTask()
        Log.d(TAG, "AbacusRestImp Requesting $request")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                logger.e(TAG, "AbacusRestImp Request Failed ${request.url}, ${e.message}")
                endBackgroundTask()
                callback(null, 0, null)
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                endBackgroundTask()
                val code = response.code
                val body = response.body?.string()
                val headersJsonString = response.headers.toMap().toJson()
                callback(body, code, headersJsonString)
            }
        })
    }

    private fun beginBackgroundTask() {
        if (backgroundTaskId == -1) {
            backgroundTaskId = AsyncTask.execute {
                // Simulate background task
            }.hashCode()
        }
    }

    private fun endBackgroundTask() {
        if (backgroundTaskId != -1) {
            // End the background task
            backgroundTaskId = -1
        }
    }
}
