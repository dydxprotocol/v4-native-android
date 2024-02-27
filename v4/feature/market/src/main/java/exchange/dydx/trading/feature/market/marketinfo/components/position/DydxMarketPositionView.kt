package exchange.dydx.trading.feature.market.marketinfo.components.position

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.dividers.PlatformVerticalDivider
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.noGradient
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.views.LeverageRiskView
import exchange.dydx.trading.feature.shared.views.SideTextView
import exchange.dydx.trading.feature.shared.views.SignedAmountView
import exchange.dydx.trading.feature.shared.views.TokenTextView
import exchange.dydx.trading.feature.shared.viewstate.SharedMarketPositionViewState

@Preview
@Composable
fun Preview_DydxMarketPositionView() {
    DydxThemedPreviewSurface {
        DydxMarketPositionView.Content(Modifier, DydxMarketPositionView.ViewState.preview)
    }
}

object DydxMarketPositionView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val shareAction: (() -> Unit)? = null,
        val closeAction: (() -> Unit)? = null,
        val sharedMarketPositionViewState: SharedMarketPositionViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                shareAction = {},
                closeAction = {},
                sharedMarketPositionViewState = SharedMarketPositionViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxMarketPositionViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            CreateCollection(Modifier, state)

            CreateButtons(Modifier, state)

            CreateList(Modifier, state)
        }
    }

    @Composable
    private fun CreateButtons(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PlatformButton(
                text = state.localizer.localize("APP.GENERAL.SHARE"),
                state = PlatformButtonState.Disabled,
                modifier = Modifier
                    .padding(vertical = ThemeShapes.VerticalPadding)
                    .weight(1f),
                action = state.shareAction ?: {},
            )

            Spacer(modifier = Modifier.width(ThemeShapes.HorizontalPadding))

            PlatformButton(
                text = state.localizer.localize("APP.TRADE.CLOSE_POSITION"),
                state = PlatformButtonState.Destructive,
                modifier = Modifier
                    .padding(vertical = ThemeShapes.VerticalPadding)
                    .weight(1f),
                action = state.closeAction ?: {},
            )
        }
    }

    @Composable
    private fun CreateList(modifier: Modifier, state: ViewState) {
        Column(
            modifier = modifier
                .padding(horizontal = ThemeShapes.HorizontalPadding)
                .padding(vertical = ThemeShapes.VerticalPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            CreateListItem(
                modifier = Modifier,
                title = state.localizer.localize("APP.TRADE.AVERAGE_OPEN"),
                textValue = state.sharedMarketPositionViewState?.entryPrice,
            )

            PlatformDivider()

            CreateListItem(
                modifier = Modifier,
                title = state.localizer.localize("APP.TRADE.AVERAGE_CLOSE"),
                textValue = state.sharedMarketPositionViewState?.exitPrice,
            )

            PlatformDivider()

            CreateListItem(
                modifier = Modifier,
                title = state.localizer.localize("APP.TRADE.NET_FUNDING"),
                value = {
                    SignedAmountView.Content(
                        modifier = Modifier,
                        state = state.sharedMarketPositionViewState?.funding,
                        textStyle = TextStyle.dydxDefault
                            .themeFont(fontType = ThemeFont.FontType.plus),
                    )
                },
            )
        }
    }

    @Composable
    private fun CreateListItem(modifier: Modifier, title: String, textValue: String?) {
        CreateListItem(modifier, title) {
            Text(
                text = textValue ?: "-",
                style = TextStyle.dydxDefault
                    .themeFont(fontType = ThemeFont.FontType.plus),
            )
        }
    }

    @Composable
    private fun CreateListItem(modifier: Modifier, title: String, value: (@Composable () -> Unit)?) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = ThemeShapes.VerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                modifier = Modifier
                    .weight(1f),
            )

            value?.invoke()
        }
    }

    @Composable
    private fun CreateCollection(modifier: Modifier, state: ViewState) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = ThemeShapes.HorizontalPadding)
                        .padding(vertical = ThemeShapes.VerticalPadding)
                        .background(
                            brush = state.sharedMarketPositionViewState?.gradientType?.brush(ThemeColor.SemanticColor.layer_3)
                                ?: ThemeColor.SemanticColor.layer_3.noGradient,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .fillMaxSize(),
                ) {
                    CreatePositionTab(
                        modifier = Modifier,
                        state = state,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    CreateCollectionItem(
                        modifier = Modifier
                            .padding(horizontal = ThemeShapes.HorizontalPadding)
                            .padding(vertical = ThemeShapes.VerticalPadding)
                            .weight(1f),
                        title = state.localizer.localize("APP.GENERAL.LEVERAGE"),
                        valueItem = {
                            Row(
                                modifier = Modifier,
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
                            ) {
                                Text(
                                    text = state.sharedMarketPositionViewState?.leverage ?: "-",
                                    style = TextStyle.dydxDefault,
                                )
                                LeverageRiskView.Content(
                                    modifier = Modifier
                                        .padding(start = ThemeShapes.HorizontalPadding),
                                    state = state.sharedMarketPositionViewState?.leverageIcon,
                                )
                            }
                        },
                    )

                    PlatformDivider()

                    CreateCollectionItem(
                        modifier = Modifier
                            .padding(horizontal = ThemeShapes.HorizontalPadding)
                            .padding(vertical = ThemeShapes.VerticalPadding)
                            .weight(1f),
                        title = state.localizer.localize("APP.TRADE.LIQUIDATION_PRICE"),
                        stringValue = state.sharedMarketPositionViewState?.liquidationPrice,
                    )
                }
            }

            PlatformDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                CreateCollectionItem(
                    modifier = Modifier
                        .padding(horizontal = ThemeShapes.HorizontalPadding)
                        .padding(vertical = ThemeShapes.VerticalPadding)
                        .weight(1f),
                    title = state.localizer.localize("APP.TRADE.UNREALIZED_PNL"),
                    valueItem = {
                        SignedAmountView.Content(
                            modifier = Modifier,
                            state = state.sharedMarketPositionViewState?.unrealizedPNLAmount,
                        )

                        SignedAmountView.Content(
                            modifier = Modifier,
                            state = state.sharedMarketPositionViewState?.unrealizedPNLPercent,
                            textStyle = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.small),
                        )
                    },
                )

                PlatformVerticalDivider()

                CreateCollectionItem(
                    modifier = Modifier
                        .padding(horizontal = ThemeShapes.HorizontalPadding)
                        .padding(vertical = ThemeShapes.VerticalPadding)
                        .weight(1f),
                    title = state.localizer.localize("APP.TRADE.REALIZED_PNL"),
                    valueItem = {
                        SignedAmountView.Content(
                            modifier = Modifier,
                            state = state.sharedMarketPositionViewState?.realizedPNLAmount,
                        )
                    },
                )
            }
        }
    }

    @Composable
    private fun CreatePositionTab(modifier: Modifier, state: ViewState) {
        Column(
            modifier = modifier
                .padding(horizontal = ThemeShapes.HorizontalPadding)
                .padding(vertical = ThemeShapes.VerticalPadding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ThemeShapes.VerticalPadding),
            ) {
                PlatformRoundImage(
                    icon = state.sharedMarketPositionViewState?.logoUrl,
                    size = 32.dp,
                )

                Spacer(modifier = Modifier.height(ThemeShapes.VerticalPadding))

                SideTextView.Content(
                    modifier = Modifier,
                    state = state.sharedMarketPositionViewState?.side,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(
                    text = state.sharedMarketPositionViewState?.size ?: "-",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium, fontType = ThemeFont.FontType.plus),
                )

                TokenTextView.Content(
                    modifier = Modifier,
                    state = state.sharedMarketPositionViewState?.token,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.tiny, fontType = ThemeFont.FontType.plus),
                )
            }

            Text(
                text = state.sharedMarketPositionViewState?.amount ?: "-",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }

    @Composable
    private fun CreateCollectionItem(modifier: Modifier, title: String?, stringValue: String?) {
        Column(
            modifier = modifier,
        ) {
            Text(
                text = title ?: "",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Spacer(modifier = Modifier.height(ThemeShapes.VerticalPadding))

            Text(
                text = stringValue ?: "-",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.medium),
            )
        }
    }

    @Composable
    private fun CreateCollectionItem(modifier: Modifier, title: String?, valueItem: @Composable () -> Unit) {
        Column(
            modifier = modifier,
        ) {
            Text(
                text = title ?: "",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )

            Spacer(modifier = Modifier.height(ThemeShapes.VerticalPadding))

            valueItem()
        }
    }
}
