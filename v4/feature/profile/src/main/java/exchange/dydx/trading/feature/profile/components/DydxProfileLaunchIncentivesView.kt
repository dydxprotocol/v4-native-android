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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
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
import exchange.dydx.trading.common.component.DydxComponent
import exchange.dydx.trading.common.compose.collectAsStateWithLifecycle
import exchange.dydx.trading.common.theme.DydxThemedPreviewSurface
import exchange.dydx.trading.common.theme.MockLocalizer
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
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = state.localizer.localize("APP.TRADING_REWARDS.LAUNCH_INCENTIVES"),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary)
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                )
                Spacer(modifier = Modifier.width(ThemeShapes.HorizontalPadding))
                Text(
                    text = state.localizer.localize("APP.PORTFOLIO.FOR_V4"),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_tertiary)
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                )
            }

            Row(
                modifier = modifier
                    .padding(vertical = ThemeShapes.VerticalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = state.localizer.localize("APP.TRADING_REWARDS.LAUNCH_INCENTIVES_DESCRIPTION"),
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
                Spacer(modifier = Modifier.width(4.dp))
                PlatformImage(
                    modifier = modifier.height(16.dp),
                    icon =
                    R.drawable.chaoslabs_logo,
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
        val seasonText = localizer.localize("APP.LEAGUES.SEASON")
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
                    .alpha(0.7f)
                    .align(Alignment.CenterEnd),
            )

            Column(
                modifier = Modifier
                    .matchParentSize()
                    .padding(16.dp),
            ) {
                Text(
                    text = localizer.localize("APP.PORTFOLIO.ESTIMATED_REWARDS"),
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary)
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                )
                Text(
                    text = "$seasonText $season",
                    style = TextStyle.dydxDefault
                        .themeColor(ThemeColor.SemanticColor.text_primary)
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
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
