package exchange.dydx.trading.feature.shared.bottombar

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
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
import exchange.dydx.trading.common.navigation.PortfolioRoutes
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxBottomBar() {
    DydxThemedPreviewSurface {
        DydxBottomBar.Content(Modifier, DydxBottomBar.ViewState.preview)
    }
}

@Composable
fun DydxBottomBarScaffold(
    modifier: Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        backgroundColor = ThemeColor.SemanticColor.layer_2.color,
        scaffoldState = rememberScaffoldState(),
        bottomBar = { DydxBottomBar.Content(modifier) },
    ) {
        content(modifier.padding(it))
    }
}

object DydxBottomBar : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val items: List<BottomBarItem>,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                items = listOf(
                    BottomBarItem.preview,
                    BottomBarItem.preview,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxBottomBarModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Column(
            modifier = modifier.themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            PlatformDivider()

            BottomNavigation(
                modifier = Modifier.padding(top = ThemeShapes.VerticalPadding),
                backgroundColor = ThemeColor.SemanticColor.layer_2.color,
                elevation = 0.dp,
            ) {
                state.items.forEach { barItem ->
                    val itemModifier = Modifier.weight(1f)

                    if (barItem !== state.items.first()) {
                        Image(
                            painter = painterResource(id = exchange.dydx.trading.common.R.drawable.tab_divider),
                            contentDescription = "",
                        )
                    }

                    if (barItem.centerButton) {
                        CenterButton(
                            modifier = itemModifier,
                            barItem = barItem,
                        )
                    } else {
                        DydxBottomBarItem(
                            modifier = itemModifier,
                            barItem = barItem,
                            state = state,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun RowScope.DydxBottomBarItem(
        modifier: Modifier,
        barItem: BottomBarItem,
        state: ViewState,
    ) {
        val iconPainter = barItem.icon?.let { painterResource(id = barItem.icon) }

        BottomNavigationItem(
            modifier = modifier,
            selected = barItem.selected,
            icon = {
                iconPainter?.let {
                    Icon(
                        modifier = Modifier.padding(bottom = ThemeShapes.VerticalPadding),
                        painter = it,
                        contentDescription = "",
                        tint = if (barItem.selected) ThemeColor.SemanticColor.text_primary.color else ThemeColor.SemanticColor.text_tertiary.color,
                    )
                }
            },
            label = {
                Text(
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.mini)
                        .themeColor(if (barItem.selected) ThemeColor.SemanticColor.text_primary else ThemeColor.SemanticColor.text_tertiary),
                    text = if (barItem.label != null) state.localizer.localize(barItem.label) else "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            onClick = {
                if (!barItem.selected) {
                    barItem.onTapAction?.invoke()
                }
            },
        )
    }

    @Composable
    private fun CenterButton(
        modifier: Modifier,
        barItem: BottomBarItem,
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = ThemeShapes.VerticalPadding)
                .padding(bottom = ThemeShapes.VerticalPadding)
                .size(50.dp)
                .clip(CircleShape)
                .clickable {
                    barItem.onTapAction?.invoke()
                },
            shape = CircleShape,
            color = ThemeColor.SemanticColor.color_purple.color,
        ) {
            Icon(
                modifier = modifier
                    .size(24.dp)
                    .padding(12.dp),
                painter = painterResource(id = R.drawable.ic_up_down_arrow),
                contentDescription = "",
                tint = ThemeColor.SemanticColor.color_white.color,
            )
        }
    }
}

data class BottomBarItem(
    val route: String?,
    val label: String?,
    val icon: Int?,
    val selected: Boolean = false,
    val centerButton: Boolean = false,
    val onTapAction: (() -> Unit)? = null,
) {
    companion object {
        val preview = BottomBarItem(
            route = PortfolioRoutes.main,
            label = "APP.PORTFOLIO.PORTFOLIO",
            icon = R.drawable.ic_tap_portfolio,
            selected = false,
        )
    }
}
