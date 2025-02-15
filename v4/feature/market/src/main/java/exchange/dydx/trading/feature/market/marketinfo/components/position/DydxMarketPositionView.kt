package exchange.dydx.trading.feature.market.marketinfo.components.position

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.localizedString
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.dividers.PlatformVerticalDivider
import exchange.dydx.platformui.components.icons.PlatformRoundImage
import exchange.dydx.platformui.compose.collectAsStateWithLifecycle
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.noGradient
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.portfolio.components.pendingpositions.DydxPortfolioPendingPositionItemView
import exchange.dydx.trading.feature.portfolio.components.placeholder.DydxPortfolioPlaceholderView
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
        val marginEditAction: (() -> Unit)? = null,
        val sharedMarketPositionViewState: SharedMarketPositionViewState? = null,
        val pendingPosition: DydxPortfolioPendingPositionItemView.ViewState? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                shareAction = {},
                closeAction = {},
                sharedMarketPositionViewState = SharedMarketPositionViewState.preview,
                pendingPosition = DydxPortfolioPendingPositionItemView.ViewState.preview,
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
            if (state.sharedMarketPositionViewState == null) {
                // No position; show placeholder
                DydxPortfolioPlaceholderView.Content(Modifier)
            } else {
                // Show position details
                CreateCollection(Modifier, state)

                DydxMarketPositionButtonsView.Content(Modifier)

                CreateList(Modifier, state)
            }

            state.pendingPosition?.let {
                // Show pending position
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = state.localizer.localize("APP.TRADE.UNOPENED_ISOLATED_POSITIONS"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.large)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )

                DydxPortfolioPendingPositionItemView.Content(
                    modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                    state = it,
                )
            }
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
                modifier = modifier
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
                            brush = state.sharedMarketPositionViewState?.gradientType?.brush(
                                ThemeColor.SemanticColor.layer_3,
                            )
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
                        title = state.localizer.localize("APP.TRADE.POSITION_LEVERAGE"),
                        valueItem = {
                            Row(
                                modifier = Modifier.sizeIn(minHeight = 32.dp),
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
                            modifier = Modifier.sizeIn(minHeight = 32.dp),
                            state = state.sharedMarketPositionViewState?.realizedPNLAmount,
                        )
                    },
                )

                PlatformVerticalDivider()

                CreateCollectionItem(
                    modifier = Modifier
                        .padding(horizontal = ThemeShapes.HorizontalPadding)
                        .padding(vertical = ThemeShapes.VerticalPadding)
                        .weight(1f),
                    title = state.localizer.localize("APP.GENERAL.MARGIN"),
                    valueItem = {
                        Row(
                            modifier = Modifier.sizeIn(minHeight = 32.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = state.sharedMarketPositionViewState?.margin ?: "-",
                                style = TextStyle.dydxDefault
                                    .themeFont(fontSize = ThemeFont.FontSize.base)
                                    .themeColor(ThemeColor.SemanticColor.text_secondary),
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            if (state.marginEditAction != null) {
                                Icon(
                                    painter = painterResource(id = exchange.dydx.trading.feature.shared.R.drawable.icon_edit),
                                    contentDescription = "",
                                    tint = ThemeColor.SemanticColor.text_secondary.color,
                                    modifier = Modifier.size(20.dp)
                                        .clickable {
                                            state.marginEditAction.invoke()
                                        },
                                )
                            }
                        }
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
                    modifier = Modifier.padding(end = 4.dp),
                    icon = state.sharedMarketPositionViewState?.logoUrl,
                    size = 32.dp,
                )

                Spacer(modifier = Modifier.weight(1f))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = state.sharedMarketPositionViewState?.size ?: "-",
                            style = TextStyle.dydxDefault
                                .themeFont(
                                    fontSize = ThemeFont.FontSize.medium,
                                    fontType = ThemeFont.FontType.plus,
                                ),
                        )

                        TokenTextView.Content(
                            modifier = Modifier,
                            state = state.sharedMarketPositionViewState?.token,
                            textStyle = TextStyle.dydxDefault
                                .themeFont(
                                    fontSize = ThemeFont.FontSize.tiny,
                                    fontType = ThemeFont.FontType.plus,
                                ),
                        )
                    }

                    Text(
                        text = state.sharedMarketPositionViewState?.notionalTotal ?: "-",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small)
                            .themeColor(ThemeColor.SemanticColor.text_tertiary),
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = ThemeShapes.VerticalPadding),
            ) {
                SideTextView.Content(
                    modifier = Modifier,
                    state = state.sharedMarketPositionViewState?.side,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )

                Spacer(modifier = Modifier.weight(1f))

                MarginMode(
                    modifier = Modifier,
                    marginMode = state.sharedMarketPositionViewState?.marginMode ?: MarginMode.Cross,
                    localizer = state.localizer,
                )
            }
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

    @Composable
    private fun MarginMode(
        modifier: Modifier,
        marginMode: MarginMode,
        localizer: LocalizerProtocol
    ) {
        marginMode.localizedString(localizer)?.let {
            Column(
                modifier = modifier
                    .background(
                        color = ThemeColor.SemanticColor.layer_7.color,
                        shape = RoundedCornerShape(4.dp),
                    )
                    .padding(horizontal = 4.dp)
                    .padding(vertical = 2.dp),
            ) {
                Text(
                    modifier = Modifier,
                    text = it,
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
            }
        }
    }
}
