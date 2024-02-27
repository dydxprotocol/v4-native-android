package ${PACKAGE_NAME}

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_${NAME}() {
    DydxThemedPreviewSurface {
        ${NAME}.Content(Modifier, ${NAME}.ViewState.preview)
    }
}

object ${NAME} : DydxComponent {
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
        val viewModel: ${NAME}Model = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }
        Text(text = state?.text ?: "")
    }
}

