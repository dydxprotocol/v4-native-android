package exchange.dydx.trading.feature.transfer.deposit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.components.icons.PlatformRoundImage
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
import java.net.URL

@Preview
@Composable
fun Preview_DydxTransferTurnkeyDepositView() {
    DydxThemedPreviewSurface {
        DydxTransferTurnkeyDepositView.Content(
            Modifier,
            DydxTransferTurnkeyDepositView.ViewState.preview,
        )
    }
}

object DydxTransferTurnkeyDepositView : DydxComponent {
    data class Item(
        val title: String,
        val subtitle: String,
        val tag: String,
        val iconUrl: String?,
        val action: () -> Unit,
    )

    data class ViewState(
        val localizer: LocalizerProtocol,
        val items: List<Item> = emptyList(),
        val closeAction: (() -> Unit)? = null,
        val fiatAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                closeAction = {},
                fiatAction = {},
                items = listOf(
                    Item(
                        title = "Deposit USDC",
                        subtitle = "Instantly deposit USDC to your dYdX account",
                        tag = "USDC",
                        iconUrl = URL("https://example.com/icon.png").toString(),
                        action = {},
                    ),
                    Item(
                        title = "Deposit ETH",
                        subtitle = "Instantly deposit ETH to your dYdX account",
                        tag = "ETH",
                        iconUrl = null,
                        action = {},
                    ),
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxTransferTurnkeyDepositViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier.fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            if (state.closeAction != null) {
                HeaderView(
                    title = state.localizer.localize("APP.GENERAL.DEPOSIT"),
                    closeAction = { state.closeAction.invoke() },
                )

                PlatformDivider()
            }

            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                for (item in state.items) {
                    item {
                        ItemContent(
                            modifier = Modifier.fillMaxWidth(),
                            item = item,
                        )
                    }
                }
            }

            if (state.fiatAction != null) {
                OrDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    state = state,
                )

                FiatItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    state = state,
                )
            }
        }
    }

    @Composable
    private fun OrDivider(
        modifier: Modifier = Modifier,
        state: ViewState,
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlatformDivider(
                modifier = Modifier.weight(1f),
            )

            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = state.localizer.localize("APP.GENERAL.OR"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.tiny)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            PlatformDivider(
                modifier = Modifier.weight(1f),
            )
        }
    }

    @Composable
    private fun FiatItem(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier
                .clickable { state.fiatAction?.invoke() }
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .themeColor(background = ThemeColor.SemanticColor.layer_4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                PlatformRoundImage(
                    icon = R.drawable.icon_cash,
                    size = 20.dp,
                )
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = state.localizer.localize("APP.GENERAL.DEPOSIT_WITH_FIAT"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )
                PlatformImage(
                    icon = R.drawable.icon_arrow_right,
                    modifier = Modifier
                        .size(20.dp),
                    colorFilter = ColorFilter.tint(color = ThemeColor.SemanticColor.text_primary.color),
                )
            }
        }
    }

    @Composable
    private fun ItemContent(
        modifier: Modifier = Modifier,
        item: Item,
    ) {
        Row(
            modifier = modifier
                .clickable { item.action() }
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
        ) {
            if (item.iconUrl != null) {
                PlatformRoundImage(
                    icon = item.iconUrl,
                    size = 32.dp,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.title,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.base)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )
                Text(
                    text = item.subtitle,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small)
                        .themeColor(ThemeColor.SemanticColor.text_tertiary),
                )
            }

            Text(
                modifier = Modifier
                    .background(
                        color = ThemeColor.SemanticColor.color_faded_purple.color,
                        shape = RoundedCornerShape(4.dp),
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                text = item.tag,
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.tiny)
                    .themeColor(ThemeColor.SemanticColor.color_purple),
            )

            Icon(
                painter = painterResource(id = R.drawable.chevron_right),
                contentDescription = "",
                modifier = Modifier.size(16.dp),
                tint = ThemeColor.SemanticColor.text_secondary.color,
            )
        }
    }
}
