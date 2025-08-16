package exchange.dydx.trading.feature.shared.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.components.buttons.PlatformIconButton
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.trading.feature.shared.R

@Composable
fun HeaderView(
    modifier: Modifier = Modifier,
    icon: Any? = null,
    title: String,
    backAction: (() -> Unit)? = null,
    closeAction: (() -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(
                //  horizontal = ThemeShapes.HorizontalPadding,
                vertical = ThemeShapes.VerticalPadding,
            )
            .padding(top = ThemeShapes.VerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        if (backAction != null) {
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                PlatformIconButton(
                    action = { backAction.invoke() },
                    backgroundColor = ThemeColor.SemanticColor.transparent,
                    borderColor = ThemeColor.SemanticColor.transparent,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.chevron_left),
                        contentDescription = "",
                        modifier = Modifier.size(18.dp),
                        tint = ThemeColor.SemanticColor.text_primary.color,
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.size(ThemeShapes.HorizontalPadding))
        }

        if (icon != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = ThemeShapes.HorizontalPadding),
            ) {
                PlatformImage(
                    icon = icon,
                    modifier = Modifier
                        .size(26.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
        ) {
            Text(
                text = title,
                style = TextStyle.dydxDefault
                    .themeFont(fontType = ThemeFont.FontType.plus, fontSize = ThemeFont.FontSize.extra)
                    .themeColor(ThemeColor.SemanticColor.text_primary),
            )
        }

        if (closeAction != null) {
            HeaderViewCloseBotton(closeAction = closeAction)
        }
    }
}

@Composable
fun RowScope.HeaderViewCloseBotton(
    modifier: Modifier = Modifier,
    closeAction: (() -> Unit)? = null,
) {
    Column(modifier = modifier.align(Alignment.CenterVertically)) {
        PlatformIconButton(
            action = { closeAction?.invoke() },
            backgroundColor = ThemeColor.SemanticColor.transparent,
            borderColor = ThemeColor.SemanticColor.transparent,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.close),
                contentDescription = "",
                modifier = Modifier.size(20.dp),
                tint = ThemeColor.SemanticColor.text_tertiary.color,
            )
        }
    }
}
