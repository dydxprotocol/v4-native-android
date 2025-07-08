package exchange.dydx.trading.feature.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformIconButton
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
        val sourceAddress: String? = null,
        val copyAddressAction: (() -> Unit)? = null,
        val blockExplorerAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                dydxAddress = "dydx1111111111111111111111122233333",
                sourceAddress = "0x111111111111111111111111222222222",
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

        Column(
            modifier = modifier
                .fillMaxWidth()
                .offset(y = (16).dp),
        ) {
            TopContent(modifier = Modifier.zIndex(1f), state = state)

            val shape = RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp)
            Column(
                modifier = Modifier
                    .offset(y = (-16).dp)
                    .background(color = ThemeColor.SemanticColor.layer_1.color, shape = shape)
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(vertical = ThemeShapes.HorizontalPadding)
                    .padding(top = 16.dp),
            ) {
                BottomContent(modifier = Modifier, state = state)
            }
        }
    }

    @Composable
    private fun TopContent(
        modifier: Modifier,
        state: ViewState,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = ThemeColor.SemanticColor.layer_3.color,
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.HorizontalPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlatformImage(
                icon = if (state.dydxAddress != null) state.dydxChainLogoUrl else R.drawable.hedgie_placholder,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                CreateAddress(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    state = state,
                )

                if (state.dydxAddress != null) {
                    Spacer(modifier = Modifier.width(ThemeShapes.HorizontalPadding))

                    CreateButtons(
                        modifier = Modifier.align(Alignment.CenterVertically).width(92.dp),
                        state = state,
                    )
                }
            }
        }
    }

    @Composable
    private fun BottomContent(
        modifier: Modifier,
        state: ViewState,
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = state.localizer.localize("APP.V4.SOURCE_ADDRESS"),
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_tertiary)
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = truncateAddress(state.sourceAddress ?: "-"),
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_secondary),
                modifier = Modifier,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    @Composable
    private fun CreateAddress(
        modifier: Modifier,
        state: ViewState,
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = state.localizer.localize("APP.V4.DYDX_ADDRESS"),
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_tertiary)
                    .themeFont(
                        fontSize = ThemeFont.FontSize.small,
                    ),
            )

            Text(
                text = truncateAddress(state.dydxAddress ?: "-"),
                style = TextStyle.dydxDefault
                    .themeColor(ThemeColor.SemanticColor.text_primary)
                    .themeFont(fontSize = ThemeFont.FontSize.large),
                modifier = Modifier,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    private fun truncateAddress(address: String): String {
        return if (address.length > 20) {
            address.take(8) + "..." + address.takeLast(6)
        } else {
            address
        }
    }

    @Composable
    private fun CreateButtons(
        modifier: Modifier,
        state: ViewState,
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CreateButton(
                modifier = Modifier.weight(1f),
                icon = R.drawable.icon_copy,
                action = state.copyAddressAction,
            )

            CreateButton(
                modifier = Modifier.weight(1f),
                icon = R.drawable.icon_external_link,
                action = state.blockExplorerAction,
            )
        }
    }

    @Composable
    private fun CreateButton(
        modifier: Modifier,
        icon: Any,
        tint: ThemeColor.SemanticColor? = ThemeColor.SemanticColor.text_secondary,
        action: (() -> Unit)?,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier,
        ) {
            PlatformIconButton(
                size = 40.dp,
                padding = 0.dp,
                action = action ?: {},
            ) {
                PlatformImage(
                    icon = icon,
                    modifier = Modifier.size(18.dp),
                    colorFilter = if (tint != null) ColorFilter.tint(tint.color) else null,
                )
            }
        }
    }
}
