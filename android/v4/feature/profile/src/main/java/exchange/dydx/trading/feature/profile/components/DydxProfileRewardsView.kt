package exchange.dydx.trading.feature.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
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
fun Preview_DydxProfileRewardsView() {
    DydxThemedPreviewSurface {
        DydxProfileRewardsView.Content(Modifier, DydxProfileRewardsView.ViewState.preview)
    }
}

object DydxProfileRewardsView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val summary: DydxRewardsSummaryState?,
        val nativeTokenLogoUrl: String? = null,
        val onTapAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                summary = DydxRewardsSummaryState(
                    titleText = "Rewards",
                    rewards7DaysText = "0.01",
                    range7DaysText = "Jan 1 to Jan 7",
                    rewardsAllTimeText = "1.0",
                ),
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxProfileRewardsViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state, false)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?, detailed: Boolean = false) {
        if (state == null) {
            return
        }

        Column(
            modifier = modifier
                .clickable { state.onTapAction?.invoke() }
                .background(
                    color = ThemeColor.SemanticColor.layer_3.color,
                    shape = RoundedCornerShape(14.dp),
                ),
        ) {
            CreateHeader(
                modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                state = state,
                detailed = detailed,
            )

            PlatformDivider()

            if (detailed) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CreateLastWeekContent(
                            modifier = Modifier
                                .padding(horizontal = ThemeShapes.HorizontalPadding)
                                .padding(vertical = 16.dp),
                            state = state,
                            detailed = detailed,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CreateAllTimeContent(
                            modifier = Modifier
                                .padding(horizontal = ThemeShapes.HorizontalPadding)
                                .padding(vertical = 16.dp),
                            state = state,
                        )
                    }
                }
            } else {
                CreateLastWeekContent(
                    modifier = Modifier
                        .padding(horizontal = ThemeShapes.HorizontalPadding)
                        .padding(vertical = 16.dp),
                    state = state,
                    detailed = detailed,
                )
                CreateAllTimeContent(
                    modifier = Modifier
                        .padding(horizontal = ThemeShapes.HorizontalPadding)
                        .padding(vertical = 16.dp),
                    state = state,
                )
            }
        }
    }

    @Composable
    private fun CreateHeader(modifier: Modifier, state: ViewState, detailed: Boolean = false) {
        Row(
            modifier = modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = state.summary?.titleText ?: "",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.small),
                modifier = Modifier.weight(1f),
            )

            if (!detailed) {
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
    }

    @Composable
    private fun CreateLastWeekContent(modifier: Modifier, state: ViewState, detailed: Boolean) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = state.localizer.localize("APP.GENERAL.TIME_STRINGS.THIS_WEEK"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = state.summary?.rewards7DaysText ?: "-",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.medium)
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                    )
                    if (state.nativeTokenLogoUrl != null && state.summary?.rewards7DaysText != null) {
                        PlatformImage(
                            icon = state.nativeTokenLogoUrl,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                if (detailed) {
                    Text(
                        text = state.summary?.range7DaysText ?: "",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.small),
                    )
                }
            }
        }
    }

    @Composable
    private fun CreateAllTimeContent(modifier: Modifier, state: ViewState) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = state.localizer.localize("APP.GENERAL.TIME_STRINGS.ALL_TIME"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = state.summary?.rewardsAllTimeText ?: "-",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.medium)
                            .themeColor(ThemeColor.SemanticColor.text_primary),
                    )
                    if (state.nativeTokenLogoUrl != null && state.summary?.rewardsAllTimeText != null) {
                        PlatformImage(
                            icon = state.nativeTokenLogoUrl,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}
