package exchange.dydx.dydxstatemanager.protocolImplementations

import android.os.AsyncTask
import android.util.Log
import exchange.dydx.abacus.protocols.RestProtocol
import exchange.dydx.abacus.utils.IMap
import okhttp3.CacheControl
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class AbacusRestImp : RestProtocol {

    private val TAG = "AbacusRestImp"

    private var backgroundTaskId: Int = -1

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun delete(
        url: String,
        headers: IMap<String, String>?,
        callback: (response: String?, httpCode: Int) -> Unit,
    ) {
        processRest(url, headers, null, "DELETE", callback)
    }

    override fun get(
        url: String,
        headers: IMap<String, String>?,
        callback: (response: String?, httpCode: Int) -> Unit,
    ) {
        processRest(url, headers, null, "GET", callback)
    }

    override fun post(
        url: String,
        headers: IMap<String, String>?,
        body: String?,
        callback: (response: String?, httpCode: Int) -> Unit,
    ) {
        processRest(url, headers, body, "POST", callback)
    }

    override fun put(
        url: String,
        headers: IMap<String, String>?,
        body: String?,
        callback: (response: String?, httpCode: Int) -> Unit,
    ) {
        processRest(url, headers, body, "PUT", callback)
    }

    private fun processRest(
        url: String,
        headers: IMap<String, String>?,
        body: String?,
        verb: String,
        callback: (String?, Int) -> Unit,
    ) {
        var requestBuilder = Request.Builder()
            .url(url)

        val headers = headers?.toTypedArray()
        headers?.forEach { (key, value) ->
            requestBuilder = requestBuilder.header(key, value)
        }
        val requestBody = if (!body.isNullOrBlank()) {
            body.toRequestBody("application/json".toMediaTypeOrNull())
        } else {
            null
        }

        val request = requestBuilder
            .cacheControl(CacheControl.Builder().noStore().build())
            .method(verb, requestBody)
            .build()

        run(request, callback)
    }

    private fun run(request: Request, callback: (String?, Int) -> Unit) {
        beginBackgroundTask()
        // Log.d(TAG, "AbacusRestImp Requesting ${request.url}")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e(TAG, "AbacusRestImp Request Failed ${request.url}, ${e.message}")
                endBackgroundTask()
                callback(null, 0)
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                endBackgroundTask()
                val code = response.code
                val body = response.body?.string()
                callback(body, code)
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
