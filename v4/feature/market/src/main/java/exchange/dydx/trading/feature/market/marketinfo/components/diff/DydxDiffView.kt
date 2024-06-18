package exchange.dydx.trading.feature.market.marketinfo.components.diff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.components.changes.PlatformDirection
import exchange.dydx.platformui.components.changes.PlatformDirectionArrow
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.formatter.DydxFormatter
import exchange.dydx.trading.feature.shared.views.LeverageRiskView
import exchange.dydx.trading.feature.shared.views.MarginUsageView

@Preview
@Composable
fun Preview_DydxDiffView() {
    DydxThemedPreviewSurface {
        DydxDiffView.Content(Modifier, DydxDiffView.ViewState.preview)
    }
}

object DydxDiffView : DydxComponent {
    enum class Layout {
        STAKCED,
        LINEAR,
    }

    data class DiffState<T>(
        val current: T? = null,
        val after: T? = null,
    ) {
        companion object {
            val preview = DiffState(
                current = "Current",
                after = "After",
            )
        }
    }

    sealed class DiffType {
        class Text(val state: DiffState<out String>) : DiffType()
        class MarginUsage(val state: DiffState<MarginUsageView.ViewState>) : DiffType()
        class Leverage(val state: DiffState<LeverageRiskView.ViewState>) : DiffType()
    }

    data class ViewState(
        val formatter: DydxFormatter,
        val lableText: String?,
        val diff: DiffType? = null,
        val layout: Layout = Layout.STAKCED,
    ) {
        companion object {
            val preview = ViewState(
                formatter = DydxFormatter(),
                lableText = "Title",
                diff = DiffType.Text(DiffState.preview),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        /*
        This view should be constructed as subview inside other views,
        such as Account Details or Receipts
        Content(modifier: Modifier, state: ViewState?) is called direct from parent view
         */
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        when (state.layout) {
            Layout.STAKCED -> {
                /*
                Title
                Current
                -> After
                 */
                Column(
                    modifier = modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = state.lableText ?: "",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.mini)
                            .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary),
                        modifier = Modifier.fillMaxWidth(),
                        softWrap = false,
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        DiffCurrentContent(
                            modifier = Modifier
                                .fillMaxWidth(),
                            state = state,
                        )

                        val after = state.diff?.let {
                            when (it) {
                                is DiffType.Text -> it.state.after
                                is DiffType.MarginUsage -> it.state.after
                                is DiffType.Leverage -> it.state.after
                            }
                        }
                        if (after != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                PlatformDirectionArrow(
                                    direction = PlatformDirection.None,
                                    modifier = Modifier.size(12.dp),
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                DiffAfterContent(modifier = Modifier, state = state)
                            }
                        }
                    }
                }
            }

            Layout.LINEAR -> {
                /*
                Title: Current -> After
                 */
                Text(text = state.lableText ?: "")
                if (state.diff != null) {
                    //   Text(text = state.diff.current ?: "")
                    //   Text(text = state.diff.after ?: "")
                }
            }
        }
    }

    @Composable
    private fun DiffCurrentContent(modifier: Modifier, state: ViewState) {
        when (state.diff) {
            is DiffType.Text -> {
                Text(
                    text = state.diff.state?.current ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.base)
                        .themeColor(foreground = ThemeColor.SemanticColor.text_secondary),
                    modifier = modifier,
                )
            }
            is DiffType.MarginUsage -> {
                MarginUsageView.Content(
                    modifier = modifier,
                    state = state.diff.state?.current,
                    formatter = state.formatter,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.base)
                        .themeColor(foreground = ThemeColor.SemanticColor.text_secondary),
                )
            }
            is DiffType.Leverage -> {
                LeverageRiskView.Content(
                    modifier = modifier,
                    state = state.diff.state?.current,
                )
            }
            else -> {}
        }
    }

    @Composable
    private fun DiffAfterContent(modifier: Modifier, state: ViewState) {
        when (state.diff) {
            is DiffType.Text -> {
                Text(
                    text = state.diff.state?.after ?: "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.base)
                        .themeColor(foreground = ThemeColor.SemanticColor.text_secondary),
                    modifier = modifier,
                )
            }
            is DiffType.MarginUsage -> {
                MarginUsageView.Content(
                    modifier = modifier,
                    state = state.diff.state?.after,
                    formatter = state.formatter,
                    textStyle = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.base)
                        .themeColor(foreground = ThemeColor.SemanticColor.text_secondary),
                )
            }
            is DiffType.Leverage -> {
                LeverageRiskView.Content(
                    modifier = modifier,
                    state = state.diff.state?.after,
                )
            }
            else -> {}
        }
    }
}
