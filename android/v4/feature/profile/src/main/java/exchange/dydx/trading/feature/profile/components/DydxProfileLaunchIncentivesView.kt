package exchange.dydx.trading.feature.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontVariation.weight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.localizeWithParams
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
import exchange.dydx.platformui.components.icons.PlatformImage
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeSettings
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.isLightTheme
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_DydxProfileLaunchIncentivesView() {
    DydxThemedPreviewSurface {
        DydxProfileLaunchIncentivesView.Content(
            Modifier,
            DydxProfileLaunchIncentivesView.ViewState.preview,
        )
    }
}

object DydxProfileLaunchIncentivesView : DydxComponent {
    data class ViewState(
        val localizer: LocalizerProtocol,
        val season: String?,
        val points: String?,
        val aboutAction: () -> Unit = {},
        val leaderboardAction: () -> Unit = {},
        val isSep2025: Boolean = false,
        val rewards_dollar_amount: String = "$1M",
        val rebate_percent: String = "50%",
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                season = "3",
                points = "1.0",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxProfileLaunchIncentivesViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        Column(
            modifier = modifier
                .background(
                    color = ThemeColor.SemanticColor.layer_3.color,
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(vertical = ThemeShapes.VerticalPadding),
        ) {
            Row(
                modifier = modifier
                    .padding(vertical = ThemeShapes.VerticalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                CreateSeasonPanel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    localizer = state.localizer,
                    season = state.season ?: "-",
                    points = state.points ?: "-",
                )
            }

            Row(
                modifier = modifier
                    .padding(vertical = ThemeShapes.VerticalPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val header = if (state.isSep2025) {
                    state.localizer.localize("APP.REWARDS_SURGE_APRIL_2025.SURGE") + ": " +
                        state.localizer.localizeWithParams(
                            path = "APP.REWARDS_SURGE_APRIL_2025.SURGE_HEADLINE_SEP_2025",
                            params = mapOf(
                                "REWARD_AMOUNT" to state.rewards_dollar_amount,
                                "REBATE_PERCENT" to state.rebate_percent,
                            ),
                        )
                } else {
                    state.localizer.localize("APP.REWARDS_SURGE_APRIL_2025.SURGE_HEADLINE")
                }
                Text(
                    text = header,
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary)
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                    modifier = Modifier.weight(1f),
                )

                Text(
                    text = state.localizer.localize("APP.GENERAL.ACTIVE"),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.color_green)
                        .themeFont(fontSize = ThemeFont.FontSize.mini),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .border(
                            1.dp,
                            ThemeColor.SemanticColor.color_green.color,
                            shape = RoundedCornerShape(4.dp),
                        )
                        .padding(
                            horizontal = 6.dp,
                            vertical = 4.dp,
                        ),
                )
            }

            Row(
                modifier = modifier
                    .padding(vertical = ThemeShapes.VerticalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val body = if (state.isSep2025) {
                    state.localizer.localizeWithParams(
                        path = "APP.REWARDS_SURGE_APRIL_2025.SURGE_BODY_SEP_2025",
                        params = mapOf(
                            "REWARD_AMOUNT" to "$1M",
                            "REBATE_PERCENT" to "50%",
                        ),
                    )
                } else {
                    state.localizer.localize(
                        path = "APP.REWARDS_SURGE_APRIL_2025.SURGE_BODY",
                    )
                }
                Text(
                    text = body,
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                )
            }

            Row(
                modifier = modifier
                    .padding(vertical = ThemeShapes.VerticalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = state.localizer.localize("APP.GENERAL.POWERED_BY_ALL_CAPS"),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_secondary)
                        .themeFont(fontSize = ThemeFont.FontSize.small),
                )
                PlatformImage(
                    modifier = modifier.height(24.dp),
                    icon = R.drawable.chaoslabs_logo,
                )
            }

            Row(
                modifier = modifier
                    .padding(vertical = ThemeShapes.VerticalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                PlatformButton(
                    text = state.localizer.localize("APP.GENERAL.ABOUT"),
                    state = PlatformButtonState.Secondary,
                    modifier = Modifier
                        .padding(vertical = ThemeShapes.VerticalPadding)
                        .width(120.dp),
                    action = state.aboutAction,
                )

                Spacer(modifier = Modifier.width(ThemeShapes.HorizontalPadding))

                PlatformButton(
                    text = state.localizer.localize("APP.PORTFOLIO.LEADERBOARD"),
                    state = PlatformButtonState.Primary,
                    modifier = Modifier
                        .padding(vertical = ThemeShapes.VerticalPadding)
                        .weight(1f),
                    action = state.leaderboardAction,
                )
            }
        }
    }

    @Composable
    private fun CreateSeasonPanel(
        modifier: Modifier,
        localizer: LocalizerProtocol,
        season: String,
        points: String?,
    ) {
        val clipShape = RoundedCornerShape(10.dp)
        Box(
            modifier = modifier.fillMaxWidth()
                .height(150.dp)
                .clip(clipShape)
                .border(
                    width = 1.dp,
                    color = ThemeColor.SemanticColor.layer_5.color,
                    shape = clipShape,
                ),
        ) {
            Image(
                painterResource(id = R.drawable.texture),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(if (ThemeSettings.shared.isLightTheme()) 0.2f else 1.0f),
            )

            Image(
                painterResource(id = R.drawable.stars),
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
                    .align(Alignment.CenterEnd),
            )

            Column(
                modifier = Modifier
                    .matchParentSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = localizer.localize("APP.TRADING_REWARDS.ESTIMATED_POINTS"),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary)
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                )
                Text(
                    text = localizer.localize("APP.TRADING_REWARDS.TOTAL_POINTS"),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(fontSize = ThemeFont.FontSize.base),
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = points ?: "-",
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_primary)
                            .themeFont(
                                fontSize = ThemeFont.FontSize.extra,
                            ),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizer.localize("APP.PORTFOLIO.POINTS"),
                        style = TextStyle.dydxDefault
                            .themeColor(ThemeColor.SemanticColor.text_primary)
                            .themeFont(fontSize = ThemeFont.FontSize.large),
                    )
                }
            }
        }
    }
}
