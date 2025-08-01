package exchange.dydx.utilities.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle

fun AnnotatedString.Builder.applyLink(
    value: String,
    key: String,
    replacement: String,
    link: String?,
    linkColor: Color,
): String {
    val startIndex = value.indexOf(key)
    val endIndex = startIndex + key.length

    val beforeString = value.substring(0, startIndex)
    val afterString = value.substring(endIndex, value.length)

    val replaceString = beforeString + replacement + afterString

    addStyle(
        style = SpanStyle(
            color = linkColor,
        ),
        start = startIndex,
        end = startIndex + replacement.length,
    )
    if (link != null) {
        addStringAnnotation(
            tag = "URL",
            annotation = link,
            start = startIndex,
            end = startIndex + replacement.length,
        )
    }
    return replaceString
}
