package exchange.dydx.trading.feature.vault.depositwithdraw

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.tabgroups.PlatformPillTextGroup
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent

@Preview
@Composable
fun Preview_DydxVaultDepositWithdrawSelectionView() {
    DydxThemedPreviewSurface {
        DydxVaultDepositWithdrawSelectionView.Content(
            Modifier,
            DydxVaultDepositWithdrawSelectionView.ViewState.preview,
        )
    }
}

object DydxVaultDepositWithdrawSelectionView : DydxComponent {
    enum class Selection {
        Deposit, Withdrawal;

        val stringKey: String
            get() = when (this) {
                Deposit -> "APP.GENERAL.DEPOSIT"
                Withdrawal -> "APP.GENERAL.WITHDRAW"
            }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val selections: List<Selection> = listOf(
            Selection.Deposit,
            Selection.Withdrawal,
        ),
        val currentSelection: Selection = Selection.Deposit,
        val onSelectionChanged: (Selection) -> Unit = {},
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxVaultDepositWithdrawSelectionViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        val items = state.selections.map { selection ->
            state.localizer.localize(selection.stringKey)
        }

        PlatformPillTextGroup(
            modifier = modifier,
            items = items,
            selectedItems = items,
            itemStyle = TextStyle.dydxDefault
                .themeColor(ThemeColor.SemanticColor.text_secondary)
                .themeFont(fontType = ThemeFont.FontType.book, fontSize = ThemeFont.FontSize.large),
            selectedItemStyle = TextStyle.dydxDefault
                .themeColor(ThemeColor.SemanticColor.text_primary)
                .themeFont(fontType = ThemeFont.FontType.book, fontSize = ThemeFont.FontSize.large),
            padding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            currentSelection = state.selections.indexOf(state.currentSelection),
            scrollingEnabled = true,
            onSelectionChanged = { index ->
                state.onSelectionChanged(state.selections[index])
            },
        )
    }
}
