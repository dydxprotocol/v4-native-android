package exchange.dydx.utilities.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object EmailUtils {
    fun sendEmailWithAttachment(
        context: Context,
        fileUri: Uri?,
        email: String,
        subject: String?,
        body: String?,
        mimeType: String?,
        chooserTitle: String?
    ) {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.setType(mimeType)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        val chooser = Intent.createChooser(emailIntent, chooserTitle)
        if (chooser.resolveActivity(context.packageManager) != null) {
            chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }
}
