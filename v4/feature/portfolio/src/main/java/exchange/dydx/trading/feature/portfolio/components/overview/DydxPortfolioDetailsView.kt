package exchange.dydx.trading.feature.portfolio.components.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
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
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LeverageRiskView
import exchange.dydx.trading.feature.shared.views.MarginUsageView
import exchange.dydx.trading.feature.shared.viewstate.SharedAccountViewState

@Preview
@Composable
fun Preview_DydxPortfolioDetailsView() {
    DydxThemedPreviewSurface {
        DydxPortfolioDetailsView.Content(Modifier, DydxPortfolioDetailsView.ViewState.preview)
    }
}

object DydxPortfolioDetailsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val formatter: DydxFormatter,
        val sharedAccountViewModel: SharedAccountViewState?,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                formatter = DydxFormatter(),
                sharedAccountViewModel = SharedAccountViewState.preview,
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxPortfolioDetailsViewModel = hiltViewModel()

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
                .padding(horizontal = ThemeShapes.HorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CreateItem(
                    modifier = Modifier.weight(1f),
                    title = state.localizer.localize("APP.GENERAL.EQUITY"),
                    value = state.sharedAccountViewModel?.equity,
                )

                CreateItem(
                    modifier = Modifier.weight(1f),
                    title = state.localizer.localize("APP.GENERAL.MARGIN_USAGE"),
                ) {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        var marginUsageIcon = state.sharedAccountViewModel?.marginUsageIcon
                        if (marginUsageIcon != null) {
                            MarginUsageView.Content(
                                modifier = Modifier,
                                state = marginUsageIcon,
                                formatter = state.formatter,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        CreateValueText(Modifier, state.sharedAccountViewModel?.marginUsage)
                    }
                }

                CreateItem(
                    modifier = Modifier.weight(1f),
                    title = state.localizer.localize("APP.GENERAL.FREE_COLLATERAL"),
                    value = state.sharedAccountViewModel?.freeCollateral,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CreateItem(
                    modifier = Modifier.weight(1f),
                    title = state.localizer.localize("APP.GENERAL.BUYING_POWER"),
                    value = state.sharedAccountViewModel?.buyingPower,
                )

                CreateItem(
                    modifier = Modifier.weight(1f),
                    title = state.localizer.localize("APP.GENERAL.LEVERAGE"),
                ) {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        var leverageIcon = state.sharedAccountViewModel?.leverageIcon
                        if (leverageIcon != null) {
                            LeverageRiskView.Content(
                                modifier = Modifier,
                                state = leverageIcon.copy(
                                    displayOption = LeverageRiskView.DisplayOption.IconOnly,
                                    viewSize = 14.dp,
                                ),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        CreateValueText(Modifier, state.sharedAccountViewModel?.leverage)
                    }
                }

                CreateItem(
                    modifier = Modifier.weight(1f),
                    title = state.localizer.localize("APP.TRADE.OPEN_INTEREST"),
                    value = state.sharedAccountViewModel?.openInterest,
                )
            }
        }
    }

    @Composable
    private fun CreateItem(
        modifier: Modifier,
        title: String,
        component: @Composable () -> Unit,
    ) {
        Column(
            modifier = modifier
                .background(
                    color = ThemeColor.SemanticColor.layer_3.color,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(8.dp),
        ) {
            Text(
                text = title,
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            component()
        }
    }

    @Composable
    private fun CreateItem(
        modifier: Modifier,
        title: String,
        value: String?,
    ) {
        CreateItem(
            modifier = modifier,
            title = title,
            component = {
                CreateValueText(Modifier, value)
            },
        )
    }

    @Composable
    private fun CreateValueText(
        modifier: Modifier,
        value: String?,
    ) {
        Text(
            modifier = modifier,
            text = value ?: "-",
            style = TextStyle.dydxDefault
                .themeColor(ThemeColor.SemanticColor.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
