package exchange.dydx.trading.feature.transfer.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.views.HeaderView
import exchange.dydx.trading.feature.shared.views.ProgressStepView

@Preview
@Composable
fun Preview_DydxTransferStatusView() {
    DydxThemedPreviewSurface {
        DydxTransferStatusView.Content(Modifier, null)
    }
}

object DydxTransferStatusView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val completed: Boolean = false,
        val title: String? = null,
        val text: String? = null,
        val steps: List<ProgressStepView.ViewState> = emptyList(),
        val deleteAction: (() -> Unit)? = null,
        val closeAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                title = "Deposit initiated",
                text = "1.0M",
                steps = listOf(
                    ProgressStepView.ViewState.preview,
                    ProgressStepView.ViewState.preview,
                    ProgressStepView.ViewState.preview.copy(status = ProgressStepView.Status.Completed),
                ),
                deleteAction = {},
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTransferStatusViewModel = hiltViewModel()

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
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_3),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeaderView(
                title = state.title ?: "",
                icon = if (state.completed) R.drawable.icon_check else R.drawable.icon_clock,
                closeAction = state.closeAction,
            )

            PlatformDivider()

            Text(
                text = state.text ?: "",
                style = TextStyle.dydxDefault,
                modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                state.steps.forEach {
                    ProgressStepView(it).Content(Modifier)
                }
            }

            if (state.deleteAction != null) {
                PlatformButton(
                    text = state.localizer.localize("APP.V4.DELETE_ALERT"),
                    state = PlatformButtonState.Destructive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    state.deleteAction.invoke()
                }
            }
        }
    }
}
