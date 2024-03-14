package exchange.dydx.trading.common.logger

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.widget.Toast
import com.tonytangandroid.wood.WoodTree
import exchange.dydx.trading.common.AppConfig
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import timber.log.Timber
import timber.log.Timber.Tree
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DydxLogger"
class DydxTimberTree : Timber.DebugTree() {
    val logFlow = MutableSharedFlow<DydxLog>(
        replay = 10,
        extraBufferCapacity = 10,
        onBufferOverflow = DROP_OLDEST,
    )

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val fullTag = "dydx#$tag"
        if (priority >= AppConfig.ANDROID_LOGGING) {
            // prefix all application tags with dydx# to make logs easier to filter in console
            super.log(priority, fullTag, message, t)
        }
    }
}

@Singleton
class DydxLogger @Inject constructor() {
    val debugTree: DydxTimberTree = DydxTimberTree()
    private var woodTree: WoodTree? = null
    fun woodTree(application: Application): Tree {
        val woodTree = WoodTree(application)
            .retainDataFor(WoodTree.Period.ONE_HOUR)
            .logLevel(AppConfig.WOOD_LOGGING)
            .autoScroll(false)
            .maxLength(100000)
            .showNotification(true)
        this.woodTree = woodTree
        return woodTree
    }

    fun emailLogDb(activity: Activity) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "*/*" // this line is a must when using ACTION_CREATE_DOCUMENT
        intent.putExtra(Intent.EXTRA_TITLE, "dydxlogs.sl3")
        activity.startActivityForResult(
            intent,
            DydxLogger.DATABASE_EXPORT_CODE,
        )
    }

    fun shareDb(activity: Activity, data: Intent?) {
        val userChosenUri = data?.data
        val inStream = try {
            activity.getDatabasePath("WoodDatabase").inputStream()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e)
            return
        }
        val outStream = userChosenUri?.let { activity.contentResolver.openOutputStream(it) }

        inStream.use { input ->
            outStream.use { output ->
                output?.let { input.copyTo(it) }
                Toast.makeText(activity, "Data exported", Toast.LENGTH_LONG).show()
                val emailIntent = Intent(Intent.ACTION_SEND)
                // Set type to email
                emailIntent.type = "vnd.android.cursor.dir/email"
                emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("bobzin@dydx.exchange"))
                emailIntent.putExtra(Intent.EXTRA_STREAM, userChosenUri)
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Android debug logs")
                activity.startActivity(Intent.createChooser(emailIntent, "Send Email"))
            }
        }
    }

    companion object {
        const val DATABASE_EXPORT_CODE = 100
    }
}

data class DydxLog(val msg: String)
