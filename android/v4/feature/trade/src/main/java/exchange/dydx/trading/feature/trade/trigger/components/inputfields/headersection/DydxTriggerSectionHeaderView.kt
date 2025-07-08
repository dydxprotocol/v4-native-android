package exchange.dydx.trading.feature.trade.trigger.components.inputfields.headersection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformVerticalDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.trade.trigger.components.inputfields.DydxTriggerOrderInputType

@Preview
@Composable
fun Preview_DydxTriggerSectionHeaderView() {
    DydxThemedPreviewSurface {
        DydxTriggerSectionHeaderView.Content(
            Modifier,
            DydxTriggerSectionHeaderView.ViewState.preview,
        )
    }
}

object DydxTriggerSectionHeaderView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val label: String,
        val amount: SignedAmountView.ViewState? = null,
        var clearAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                label = "1.0M",
                amount = SignedAmountView.ViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        Content(modifier, DydxTriggerOrderInputType.TakeProfit)
    }

    @Composable
    fun Content(modifier: Modifier, inputType: DydxTriggerOrderInputType) {
        when (inputType) {
            DydxTriggerOrderInputType.TakeProfit -> {
                val viewModel: DydxTriggerSectionHeaderTakeProfitViewModel = hiltViewModel()
                val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
                Content(modifier, state)
            }

            DydxTriggerOrderInputType.StopLoss -> {
                val viewModel: DydxTriggerSectionHeaderStopLossViewModel = hiltViewModel()
                val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
                Content(modifier, state)
            }
        }
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null || state.amount == null) {
            return
        }

        val focusManager = LocalFocusManager.current

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.End,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.height(24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.label,
                    style = TextStyle.dydxDefault.themeColor(ThemeColor.SemanticColor.text_tertiary),
                )

                SignedAmountView.Content(
                    modifier = Modifier
                        .padding(start = 8.dp),
                    state = state.amount,
                )

                PlatformVerticalDivider()

                Text(
                    modifier = Modifier
                        .clickable {
                            focusManager.clearFocus()
                            state.clearAction?.invoke()
                        },
                    text = state.localizer.localize("APP.GENERAL.CLEAR"),
                    style = TextStyle.dydxDefault.themeColor(ThemeColor.SemanticColor.color_red),
                )
            }
        }
    }
}
