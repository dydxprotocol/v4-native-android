package exchange.dydx.trading.feature.receipt.validation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.navigation.DydxAnimation
import exchange.dydx.utilities.utils.toDp

@Preview
@Composable
fun Preview_DydxTradeValidationView() {
    DydxThemedPreviewSurface {
        DydxValidationView.Content(Modifier, DydxValidationView.ViewState.preview)
    }
}

object DydxValidationView : DydxComponent {

    sealed class State {
        object Error : State()
        object Warning : State()
        object None : State()
        data class Custom(
            val tabColor: ThemeColor.SemanticColor,
            val backgroundColor: ThemeColor.SemanticColor
        ) : State()
    }

    data class Link(
        val text: String,
        val action: () -> Unit,
    ) {
        companion object {
            val preview = Link(
                text = "Learn more",
                action = {},
            )
        }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val state: State = State.None,
        val title: String? = null,
        val message: String? = null,
        val link: Link? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                state = State.Warning,
                title = "This is a warning",
                message = "This is an error message",
                link = Link.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxValidationViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        DydxAnimation.AnimateExpandInOut(
            visible = when (state.state) {
                State.Error, State.Warning -> true
                State.None -> false
                is State.Custom -> true
            },
        ) {
            ContentInBox(modifier, state)
        }
    }

    @Composable
    private fun ContentInBox(
        modifier: Modifier,
        viewState: ViewState
    ) {
        if (viewState.title.isNullOrEmpty() && viewState.message.isNullOrEmpty()) {
            return
        }

        var size by remember { mutableStateOf(IntSize.Zero) }

        val shape = RoundedCornerShape(8.dp)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = when (viewState.state) {
                        State.Error -> ThemeColor.SemanticColor.color_faded_red.color
                        State.Warning -> ThemeColor.SemanticColor.color_faded_yellow.color
                        State.None -> ThemeColor.SemanticColor.layer_2.color
                        is State.Custom -> viewState.state.backgroundColor.color
                    },
                    shape = shape,
                )
                .clip(shape),
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(size.height.toDp)
                    .themeColor(
                        background = when (viewState.state) {
                            State.Error -> ThemeColor.SemanticColor.color_red
                            State.Warning -> ThemeColor.SemanticColor.color_yellow
                            State.None -> ThemeColor.SemanticColor.layer_2
                            is State.Custom -> viewState.state.tabColor
                        },
                    ),
            )
            Column(
                modifier = Modifier
                    .onSizeChanged { size = it }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (viewState.title != null) {
                    Text(
                        modifier = Modifier,
                        text = viewState.title,
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_primary)
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                }
                if (viewState.message != null) {
                    Text(
                        modifier = Modifier,
                        text = viewState.message,
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                }
                if (viewState.link != null) {
                    Text(
                        modifier = Modifier
                            .clickable {
                                viewState.link.action.invoke()
                            },
                        text = viewState.link.text,
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small)
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                    )
                }
            }
        }
    }
}
