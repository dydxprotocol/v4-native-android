package exchange.dydx.trading.feature.vault.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.views.HeaderView

@Preview
@Composable
fun Preview_DydxVaultHistoryView() {
    DydxThemedPreviewSurface {
        DydxVaultHistoryView.Content(Modifier, DydxVaultHistoryView.ViewState.preview)
    }
}

object DydxVaultHistoryView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val backAction: (() -> Unit)? = null,
        val items: List<DydxVaultHistoryItemView.ViewState> = emptyList(),
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                items = listOf(
                    DydxVaultHistoryItemView.ViewState.preview,
                    DydxVaultHistoryItemView.ViewState.preview,
                    DydxVaultHistoryItemView.ViewState.preview,
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultHistoryViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier
                .fillMaxSize(),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.VAULTS.YOUR_DEPOSITS_AND_WITHDRAWALS"),
                backAction = state.backAction,
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                stickyHeader(key = "sectionHeader") {
                    SectionHeaderContent(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        state = state,
                    )
                }

                items(state.items.size) {
                    DydxVaultHistoryItemView.Content(
                        modifier = Modifier,
                        state = state.items[it],
                    )

                    if (it != state.items.size - 1) {
                        PlatformDivider()
                    }
                }
            }
        }
    }

    @Composable
    private fun SectionHeaderContent(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier,
        ) {
            Text(
                text = state.localizer.localize("APP.GENERAL.TIME"),
                modifier = Modifier
                    .weight(1f),
                style = TextStyle.dydxDefault
                    .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary)
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )

            Text(
                text = state.localizer.localize("APP.GENERAL.ACTION"),
                modifier = Modifier
                    .weight(1f),
                style = TextStyle.dydxDefault
                    .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary)
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )

            Text(
                text = state.localizer.localize("APP.GENERAL.AMOUNT"),
                textAlign = TextAlign.End,
                modifier = Modifier
                    .weight(1f),
                style = TextStyle.dydxDefault
                    .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary)
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )
        }
    }
}
