package exchange.dydx.trading.feature.profile.components

import android.R.attr.maxLines
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxProfileHeaderView() {
    DydxThemedPreviewSurface {
        DydxProfileHeaderView.Content(Modifier, DydxProfileHeaderView.ViewState.preview)
    }
}

object DydxProfileHeaderView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val dydxChainLogoUrl: String? = null,
        val dydxAddress: String? = null,
        val onTapAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                dydxAddress = "dydx11111111111111111111111",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxProfileHeaderViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Row(
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.VerticalPadding,
                )
                .padding(top = ThemeShapes.VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterVertically),
            ) {
                PlatformImage(
                    icon = if (state.dydxAddress != null) state.dydxChainLogoUrl else R.drawable.hedgie_placholder,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                )
            }

            Spacer(modifier = Modifier.width(ThemeShapes.HorizontalPadding))

            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
            ) {
                Text(
                    text = state.localizer.localize("APP.V4.DYDX_ADDRESS"),
                    style = TextStyle.dydxDefault
                        .themeFont(
                            fontSize = ThemeFont.FontSize.small,
                        ),
                )

//                AndroidView(
//                    factory = { context ->
//                        TextView(context).apply {
//                            maxLines = 1
//                            ellipsize = TextUtils.TruncateAt.MIDDLE
//                        }
//                    },
//                    update = {
//                        it.text = state.dydxAddress ?: "-"
//                        it.setTextColor(ThemeColor.SemanticColor.text_primary.color.toArgb())
//                    }
//                )

                val address = state.dydxAddress ?: "-"
                val chunked = if (address.length > 20) {
                    address.take(12) + "..." + address.takeLast(8)
                } else {
                    address
                }
                Text(
                    text = chunked,
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                    modifier = Modifier,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (state.dydxAddress != null) {
                Spacer(modifier = Modifier.width(ThemeShapes.HorizontalPadding))

                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                    Icon(
                        painter = painterResource(id = R.drawable.chevron_right),
                        contentDescription = "",
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}
