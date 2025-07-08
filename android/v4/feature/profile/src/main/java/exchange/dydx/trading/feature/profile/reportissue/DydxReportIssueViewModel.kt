package exchange.dydx.trading.feature.profile.reportissue

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.PlatformInfoViewModel
import exchange.dydx.trading.common.DydxViewModel
import exchange.dydx.trading.common.di.CoroutineDispatchers
import exchange.dydx.trading.common.navigation.DydxRouter
import exchange.dydx.utilities.utils.EmailUtils
import exchange.dydx.utilities.utils.FileUtils
import exchange.dydx.utilities.utils.LogCatReader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DydxReportIssueViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
    private val router: DydxRouter,
    @ApplicationContext private val context: Context,
    @CoroutineDispatchers.IO private val ioDispatcher: CoroutineDispatcher,
    val toaster: PlatformInfo,
    private val logCatReader: LogCatReader,
    private val fileUtils: FileUtils,
) : ViewModel(), DydxViewModel {

    private val textFlow = MutableStateFlow("")

    val state: Flow<DydxReportIssueView.ViewState?> = textFlow.map { text ->
        createViewState(text)
    }

    init {
        textFlow.value = localizer.localize("APP.ISSUE_REPORT.LOADING_TITLE")

        viewModelScope.launch {
            var logUri: Uri? = null
            withContext(ioDispatcher) {
                // add a delay to show the loading text
                kotlinx.coroutines.delay(500)
                logUri = createLog()
            }
            if (logUri != null) {
                textFlow.value = localizer.localize("APP.ISSUE_REPORT.LOADING_COMPLETED_TITLE")
                EmailUtils.sendEmailWithAttachment(
                    context = context,
                    fileUri = logUri,
                    email = "",
                    subject = localizer.localize("APP.ISSUE_REPORT.EMAIL_SUBJECT"),
                    body = localizer.localize("APP.ISSUE_REPORT.EMAIL_BODY"),
                    mimeType = "application/x-zip",
                    chooserTitle = localizer.localize("APP.ISSUE_REPORT.CHOOSER_TITLE"),
                )
            } else {
                val error = localizer.localize("APP.ISSUE_REPORT.LOADING_ERROR_TITLE")
                textFlow.value = error
                toaster.show(
                    message = error,
                    type = PlatformInfoViewModel.Type.Error,
                )
            }

            router.navigateBack()
        }
    }

    private fun createViewState(text: String): DydxReportIssueView.ViewState {
        return DydxReportIssueView.ViewState(
            localizer = localizer,
            text = text,
        )
    }

    private fun createLog(): Uri? {
        val file =
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "dydx_app.log")
        if (!logCatReader.saveLogCatToFile(file)) {
            return null
        }
        val zipFile = createZipFile(context, file)
        file.delete()
        return if (zipFile != null) {
            FileProvider.getUriForFile(context, context.packageName, zipFile)
        } else {
            null
        }
    }

    private fun createZipFile(context: Context, file: File): File? {
        val fileName = file.name
        val zipFileName = "$fileName.zip"
        val zipFilePath =
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), zipFileName)
        return if (fileUtils.compressFile(context, file.absolutePath, zipFilePath.absolutePath)) {
            zipFilePath
        } else {
            null
        }
    }
}
