package exchange.dydx.feature.onboarding.turnkey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.HeaderViewCloseBotton
import exchange.dydx.trading.integration.react.LocalizerEntry
import exchange.dydx.trading.integration.react.ReactNativeView

@Preview
@Composable
fun Preview_DydxTurnkeyAuthView() {
    DydxThemedPreviewSurface {
        DydxTurnkeyAuthView.Content(Modifier, DydxTurnkeyAuthView.ViewState.preview)
    }
}

object DydxTurnkeyAuthView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val initialProperties: Map<String, String>?,
        val localizerEntries: List<LocalizerEntry> = emptyList(), // Optional, for localization
        val closeAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                initialProperties = mapOf("userId" to "123"),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTurnkeyAuthViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Box(Modifier.fillMaxSize()) {
            ReactNativeView(
                moduleName = "TurnkeyLogin",
                initialProps = state.initialProperties,
                localizerEntries = state.localizerEntries,
                localizer = state.localizer,
                modifier = Modifier
                    .fillMaxWidth(),
            )

            Column {
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
                    Spacer(Modifier.weight(1f))

                    HeaderViewCloseBotton(closeAction = state.closeAction)
                }

                Spacer(Modifier.weight(1f)) // fills all vertical empty space
            }
        }
    }
}
