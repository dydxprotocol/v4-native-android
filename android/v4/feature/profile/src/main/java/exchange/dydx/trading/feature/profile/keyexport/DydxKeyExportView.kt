package exchange.dydx.trading.feature.profile.keyexport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformPillButton
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R
import exchange.dydx.trading.feature.shared.views.HeaderView

@Preview
@Composable
fun Preview_DydxKeyExportView() {
    DydxThemedPreviewSurface {
        DydxKeyExportView.Content(Modifier, DydxKeyExportView.ViewState.preview)
    }
}

object DydxKeyExportView : DydxComponent {
    enum class State {
        Warning, NotRevealed, Revealed;
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val exportState: State = State.Warning,
        val closeAction: (() -> Unit)? = null,
        val copyAction: (() -> Unit)? = null,
        val stateAction: ((State) -> Unit)? = null,
        val phrase: String? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                exportState = State.NotRevealed,
                phrase = "This is a secret phrase",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxKeyExportViewModel = hiltViewModel()

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
                .themeColor(ThemeColor.SemanticColor.layer_2)
                .fillMaxSize()
                .padding(horizontal = ThemeShapes.HorizontalPadding),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.MNEMONIC_EXPORT.REVEAL_SECRET_PHRASE"),
                closeAction = { state.closeAction?.invoke() },
            )

            Text(
                modifier = Modifier.padding(ThemeShapes.VerticalPadding),
                text = state.localizer.localize("APP.MNEMONIC_EXPORT.REVEAL_SECRET_PHRASE_DESCRIPTION"),
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Row(
                modifier = Modifier.padding(vertical = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
            ) {
                PlatformImage(
                    modifier = Modifier.size(40.dp),
                    icon = R.drawable.icon_keyexport_warning,
                )

                Text(
                    modifier = Modifier,
                    text = state.localizer.localize("APP.MNEMONIC_EXPORT.SECRET_PHRASE_RISK"),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_secondary),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                val shape = RoundedCornerShape(8.dp)
                DisplayContent(
                    modifier = Modifier
                        .background(ThemeColor.SemanticColor.layer_1.color, shape)
                        .padding(horizontal = 24.dp)
                        .padding(vertical = 21.dp)
                        .padding(bottom = 8.dp),
                    state = state,
                )

                CtaButtonContent(
                    modifier = Modifier,
                    state = state,
                )
            }
        }
    }

    @Composable
    private fun CtaButtonContent(modifier: Modifier, state: ViewState) {
        val clipboardManager: ClipboardManager = LocalClipboardManager.current

        when (state.exportState) {
            State.Warning -> {
                PlatformButton(
                    modifier = modifier
                        .padding()
                        .fillMaxWidth()
                        .offset(y = (-8).dp),
                    text = state.localizer.localize("APP.MNEMONIC_EXPORT.I_UNDERSTAND"),
                    action = {
                        state.stateAction?.invoke(state.exportState)
                    },
                )
            }

            State.NotRevealed, State.Revealed -> {
                PlatformButton(
                    modifier = modifier
                        .padding()
                        .fillMaxWidth()
                        .offset(y = (-8).dp),
                    text = state.localizer.localize("APP.MNEMONIC_EXPORT.COPY_TO_CLIPBOARD"),
                    action = {
                        clipboardManager.setText(
                            AnnotatedString(state.phrase ?: ""),
                        )
                        state.copyAction?.invoke()
                    },
                )
            }
        }
    }

    @Composable
    private fun DisplayContent(modifier: Modifier, state: ViewState) {
        Box(modifier = modifier) {
            when (state.exportState) {
                State.Warning -> {
                    Column(
                        modifier = Modifier.padding(ThemeShapes.VerticalPadding),
                        verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
                    ) {
                        Text(
                            modifier = Modifier,
                            text = state.localizer.localize("APP.MNEMONIC_EXPORT.BEFORE_PROCEED"),
                            style = TextStyle.dydxDefault
                                .themeColor(ThemeColor.SemanticColor.text_primary)
                                .themeFont(fontSize = ThemeFont.FontSize.large),
                        )

                        Text(
                            modifier = Modifier,
                            text = state.localizer.localize("APP.MNEMONIC_EXPORT.BEFORE_PROCEED_ACK"),
                            style = TextStyle.dydxDefault,
                        )
                    }
                }

                State.NotRevealed -> {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            modifier = Modifier.blur(
                                radius = 8.dp,
                                edgeTreatment = BlurredEdgeTreatment.Unbounded,
                            ),
                            text = state.phrase ?: "",
                            style = TextStyle.dydxDefault,
                        )

                        PlatformPillButton(
                            modifier = Modifier.height(36.dp),
                            action = { state.stateAction?.invoke(state.exportState) },
                        ) {
                            Text(
                                text = state.localizer.localize("APP.MNEMONIC_EXPORT.TAP_TO_REVEAL"),
                                style = TextStyle.dydxDefault
                                    .themeColor(ThemeColor.SemanticColor.text_primary),
                            )
                        }
                    }
                }

                State.Revealed -> {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.phrase ?: "",
                            style = TextStyle.dydxDefault,
                        )

                        Spacer(modifier = Modifier.height(36.dp))
                    }
                }
            }
        }
    }
}
