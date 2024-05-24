package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.icons.PlatformRoundIcon
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.R
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxDebugScanView() {
    DydxThemedPreviewSurface {
        ProgressStepView(ProgressStepView.ViewState.preview)
            .Content(Modifier)
    }
}

open class ProgressStepView(
    val viewState: ViewState,
) : DydxComponent {
    sealed class Status {
        data class Custom(val value: String) : Status()
        object InProgress : Status()
        object Completed : Status()
    }

    data class ViewState(
        val title: String? = null,
        val subtitle: String? = null,
        var status: Status = Status.Custom("1"),
        val tapAction: (() -> Unit)? = null,
        val trailingIcon: Any? = null,
    ) {
        companion object {
            val preview = ViewState(
                title = "Test String",
                subtitle = "Subtitle String",
                status = Status.Custom("1"),
                trailingIcon = R.drawable.ic_done,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (viewState.tapAction != null) {
                        Modifier.clickable { viewState.tapAction?.invoke() }
                    } else {
                        Modifier
                    },
                )
                .background(
                    color = ThemeColor.SemanticColor.layer_4.color,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(ThemeShapes.HorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val iconSize = 36.dp
            when (val status = viewState.status) {
                is Status.InProgress -> CircularProgressIndicator(
                    modifier = Modifier
                        .size(iconSize)
                        .align(Alignment.CenterVertically),
                )

                is Status.Completed ->
                    PlatformRoundIcon(
                        icon = R.drawable.ic_done,
                        backgroundColor = ThemeColor.SemanticColor.layer_2,
                        borderColor = ThemeColor.SemanticColor.color_green,
                        iconTint = ThemeColor.SemanticColor.color_green,
                        size = iconSize,
                        iconSize = 18.dp,
                    )

                is Status.Custom ->
                    Box(
                        modifier = Modifier
                            .size(iconSize)
                            .align(Alignment.CenterVertically),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            style = TextStyle.dydxDefault,
                            text = status.value,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .drawBehind {
                                    drawCircle(
                                        color = ThemeColor.SemanticColor.layer_2.color,
                                        radius = 22.dp.toPx(),
                                    )
                                },
                        )
                    }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f),
            ) {
                if (viewState.title != null) {
                    Text(
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.base)
                            .themeColor(foreground = ThemeColor.SemanticColor.text_primary),
                        text = viewState.title,
                    )
                }

                if (viewState.subtitle != null) {
                    Text(
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small)
                            .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary),
                        text = viewState.subtitle,
                    )
                }
            }

            if (viewState.trailingIcon != null) {
                PlatformImage(
                    icon = viewState.trailingIcon,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}
