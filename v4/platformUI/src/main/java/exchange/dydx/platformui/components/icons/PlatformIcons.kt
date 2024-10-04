package exchange.dydx.platformui.components.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import exchange.dydx.platformui.R
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.color

@Composable
fun PlatformImage(
    modifier: Modifier = Modifier,
    icon: Any?,
    colorFilter: ColorFilter? = null,
) {
    AsyncImage(
        model = iconModel(icon),
        contentDescription = "",
        contentScale = ContentScale.Fit,
        modifier = modifier,
        colorFilter = colorFilter,
    )
}

@Composable
fun PlatformRoundImage(
    modifier: Modifier = Modifier,
    icon: Any?,
    size: Dp = 60.dp,
) {
    AsyncImage(
        model = iconModel(icon),
        contentDescription = "",
        contentScale = ContentScale.Fit, // crop the image if it's not a square
        modifier = modifier
            .size(size)
            .clip(CircleShape), // clip to the circle shape
    )
}

@Composable
private fun iconModel(icon: Any?): Any? {
    val iconString = icon.toString()
    return if (iconString.endsWith(".svg")) {
        ImageRequest.Builder(LocalContext.current)
            .data(iconString)
            .decoderFactory(SvgDecoder.Factory())
            .build()
    } else {
        icon
    }
}

@Composable
fun PlatformRoundIcon(
    icon: Any?,
    backgroundColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.layer_5,
    borderColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.transparent,
    iconTint: ThemeColor.SemanticColor? = ThemeColor.SemanticColor.text_primary,
    size: Dp = 60.dp,
    iconSize: Dp = 30.dp,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size(size)
            .background(backgroundColor.color, CircleShape)
            .border(1.dp, borderColor.color, CircleShape),
    ) {
        val colorFilter: ColorFilter? = if (iconTint != null) {
            ColorFilter
                .tint(iconTint.color)
        } else {
            null
        }
        AsyncImage(
            model = iconModel(icon),
            contentDescription = "",
            modifier = Modifier
                .size(iconSize),
            colorFilter = colorFilter,
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
fun PlatformSelectedIcon(
    backgroundColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.color_purple,
    borderColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.layer_6,
    size: Dp = 20.dp,
    padding: Dp = 4.dp,
) {
    val shape = RoundedCornerShape(7.dp)
    Image(
        painter = painterResource(id = R.drawable.icon_check),
        contentDescription = "",
        modifier = Modifier
            .background(backgroundColor.color, shape)
            .border(1.dp, borderColor.color, shape)
            .clip(shape)
            .size(size)
            .padding(padding),
        colorFilter = ColorFilter
            .tint(ThemeColor.SemanticColor.color_white.color),
        contentScale = ContentScale.Fit,
    )
}

@Composable
fun PlatformUnselectedIcon(
    backgroundColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.transparent,
    borderColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.layer_6,
    size: Dp = 20.dp,
    padding: Dp = 4.dp,
) {
    val shape = RoundedCornerShape(7.dp)
    Canvas(
        modifier = Modifier
            .border(1.dp, borderColor.color, shape)
            .clip(shape)
            .size(size),
    ) {
        drawCircle(color = backgroundColor.color)
    }
}
