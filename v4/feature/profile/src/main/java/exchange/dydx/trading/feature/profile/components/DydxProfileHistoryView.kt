package exchange.dydx.trading.feature.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
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
import exchange.dydx.trading.feature.profile.components.DydxProfileHistoryItemView.ViewState.Companion.itemRatios
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxProfileHistoryView() {
    DydxThemedPreviewSurface {
        DydxProfileHistoryView.Content(Modifier, DydxProfileHistoryView.ViewState.preview)
    }
}

object DydxProfileHistoryView : DydxComponent {

    data class ViewState(
        val localizer: LocalizerProtocol,
        val items: List<DydxProfileHistoryItemView.ViewState> = emptyList(),
        val tapAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                items = listOf(
                    DydxProfileHistoryItemView.ViewState.preview,
                    DydxProfileHistoryItemView.ViewState.preview,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxProfileHistoryViewModel = hiltViewModel()

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
                .clickable { state.tapAction?.invoke() }
                .background(
                    color = ThemeColor.SemanticColor.layer_3.color,
                    shape = RoundedCornerShape(14.dp),
                )
                .height(200.dp),
        ) {
            CreateHeader(
                modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                state = state,
            )

            PlatformDivider()

            CreateTitles(
                modifier = Modifier
                    .padding(horizontal = ThemeShapes.HorizontalPadding)
                    .padding(vertical = ThemeShapes.VerticalPadding),
                state = state,
            )

            state.items.forEachIndexed { index, item ->
                DydxProfileHistoryItemView.Content(
                    modifier = Modifier
                        .padding(horizontal = ThemeShapes.HorizontalPadding)
                        .padding(vertical = 4.dp),
                    state = item,
                )
            }
        }
    }

    @Composable
    private fun CreateHeader(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = state.localizer.localize("APP.GENERAL.HISTORY"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small),
                modifier = Modifier.weight(1f),
            )

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Icon(
                    painter = painterResource(id = R.drawable.chevron_right),
                    contentDescription = "",
                    modifier = Modifier.size(16.dp),
                    tint = ThemeColor.SemanticColor.text_secondary.color,
                )
            }
        }
    }

    @Composable
    private fun CreateTitles(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(itemRatios[0]),
                text = state.localizer.localize("APP.GENERAL.ACTION"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
            Text(
                modifier = Modifier.weight(itemRatios[1]),
                text = state.localizer.localize("APP.GENERAL.SIDE"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                textAlign = TextAlign.Center,
            )
            Text(
                modifier = Modifier.weight(itemRatios[2]),
                text = state.localizer.localize("APP.GENERAL.TYPE"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                textAlign = TextAlign.Center,
            )
            Text(
                modifier = Modifier.weight(itemRatios[3]),
                text = state.localizer.localize("APP.GENERAL.AMOUNT"),
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.mini)
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
                textAlign = TextAlign.End,
            )
        }
    }
}
