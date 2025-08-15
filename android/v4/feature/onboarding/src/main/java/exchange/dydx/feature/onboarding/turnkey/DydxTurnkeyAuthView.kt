package exchange.dydx.feature.onboarding.turnkey

import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
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
        val text: String?,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                text = "1.0M",
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

        ScreenWithRN()
    }

    @Composable
    fun ScreenWithRN() {
        Column(Modifier.fillMaxSize()) {
            Text("Compose above")
            ReactNativeView(
                moduleName = "TurnkeyLogin",
                initialProps = Bundle().apply { putString("userId", "123") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Text("Compose below")
        }
    }
}

