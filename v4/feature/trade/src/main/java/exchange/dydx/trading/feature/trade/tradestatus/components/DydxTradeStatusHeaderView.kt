package exchange.dydx.trading.feature.trade.tradestatus.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.progress.PlatformIndeterminateProgress
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton

@Preview
@Composable
fun Preview_DydxTradeStatusHeaderView() {
    DydxThemedPreviewSurface {
        DydxTradeStatusHeaderView.Content(Modifier, DydxTradeStatusHeaderView.ViewState.preview)
    }
}

object DydxTradeStatusHeaderView : DydxComponent {
    enum class StatusIcon {
        Submitting, Pending, Open, Filled, Failed
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val status: StatusIcon,
        val title: String? = null,
        val detail: String? = null,
        val closeButtonAction: () -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                status = StatusIcon.Submitting,
                title = "Check status",
                detail = "Your order has been submitted.",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTradeStatusHeaderViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier
                .padding(horizontal = ThemeShapes.HorizontalPadding)
                .padding(vertical = ThemeShapes.VerticalPadding),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
            ) {
                when (state.status) {
                    StatusIcon.Submitting -> {
                        PlatformIndeterminateProgress(
                            size = 36.dp,
                        )
                    }
                    StatusIcon.Pending -> {
                        PlatformIndeterminateProgress(
                            size = 36.dp,
                        )
                    }
                    StatusIcon.Open -> {
                        PlatformImage(
                            modifier = Modifier
                                .size(36.dp),
                            icon = R.drawable.status_complete,
                        )
                    }
                    StatusIcon.Filled -> {
                        PlatformImage(
                            modifier = Modifier
                                .size(36.dp),
                            icon = R.drawable.status_complete,
                        )
                    }
                    StatusIcon.Failed -> {
                        PlatformImage(
                            modifier = Modifier
                                .size(36.dp),
                            icon = R.drawable.status_warning,
                        )
                    }
                }

                Text(
                    text = state.title ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontType = ThemeFont.FontType.plus, fontSize = ThemeFont.FontSize.extra)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                Spacer(modifier = Modifier.weight(1f))

                HeaderViewCloseBotton(closeAction = state.closeButtonAction)
            }

            Text(
                text = state.detail ?: "",
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }
}
