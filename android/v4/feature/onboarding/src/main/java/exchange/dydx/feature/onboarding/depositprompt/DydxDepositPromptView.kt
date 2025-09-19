package exchange.dydx.feature.onboarding.depositprompt

import android.R.attr.shape
import android.R.attr.text
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.components.buttons.PlatformButtonState
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
import exchange.dydx.trading.feature.shared.views.HeaderView

@Preview
@Composable
fun Preview_DydxDepositPromptView() {
    DydxThemedPreviewSurface {
        DydxDepositPromptView.Content(Modifier, DydxDepositPromptView.ViewState.preview)
    }
}

object DydxDepositPromptView : DydxComponent {
    enum class LoginMode {
        apple, google, email;

        companion object {
            fun fromString(value: String?): LoginMode? {
                return when (value?.lowercase()) {
                    "apple" -> apple
                    "google" -> google
                    "email" -> email
                    else -> null
                }
            }
        }

        val icon: Int
            @Composable get() = when (this) {
                apple -> R.drawable.icon_apple
                google -> R.drawable.icon_google
                email -> R.drawable.icon_email_2
            }

        val colorFilter: ColorFilter?
            @Composable get() = when (this) {
                apple -> ColorFilter.tint(ThemeColor.SemanticColor.text_primary.color)
                google -> null // Keep original colors for Google icon
                email -> ColorFilter.tint(ThemeColor.SemanticColor.text_primary.color)
            }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val loginMode: LoginMode? = null,
        val user: String?,
        val ctaAction: (() -> Unit)? = null,
        val closeAction: (() -> Unit)? = null,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                user = "Apple User",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxDepositPromptViewModel = hiltViewModel()

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
                .themeColor(background = ThemeColor.SemanticColor.layer_2)
                .fillMaxSize()
                .padding(
                    vertical = ThemeShapes.VerticalPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.ONBOARDING.WELCOME"),
                closeAction = state.closeAction,
            )

            PlatformDivider()

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally, // optional, centers horizontally
            ) {
                WelcomeContent(modifier = Modifier, state = state)
            }

            PlatformButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = ThemeShapes.HorizontalPadding,
                        vertical = ThemeShapes.VerticalPadding,
                    ),
                text = state.localizer.localize("APP.TURNKEY_ONBOARD.DEPOSIT_AND_TRADE"),
                state = PlatformButtonState.Primary,
                action = state.ctaAction ?: {},
            )
        }
    }

    @Composable
    private fun WelcomeContent(
        modifier: Modifier,
        state: ViewState,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.VerticalPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.stars),
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp),
            )

            Column(
                modifier = modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.localizer.localize("APP.TURNKEY_ONBOARD.WELCOME_TO_DYDX"),
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                ThemeColor.SemanticColor.text_primary.color,
                                ThemeColor.SemanticColor.color_purple.color,
                            ),
                        ),
                    )
                        .themeFont(
                            fontType = ThemeFont.FontType.plus,
                            fontSize = ThemeFont.FontSize.large,
                        ),
                )

                Text(
                    text = state.localizer.localize("APP.TURNKEY_ONBOARD.USER_SIGNED_IN_BELOW"),
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.medium)
                        .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary),
                )
            }

            Box(
                modifier = Modifier.height(48.dp),
            ) {
                if (!state.user.isNullOrEmpty()) {
                    val shape = RoundedCornerShape(percent = 50)
                    Row(
                        modifier = Modifier
                            .background(color = ThemeColor.SemanticColor.layer_3.color, shape = shape)
                            .border(1.dp, ThemeColor.SemanticColor.layer_4.color, shape = shape)
                            .padding(vertical = 8.dp)
                            .padding(horizontal = 16.dp)
                            .clip(shape),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (state.loginMode != null) {
                            PlatformImage(
                                icon = state.loginMode.icon,
                                modifier = Modifier
                                    .size(16.dp),
                                colorFilter = state.loginMode.colorFilter,
                            )
                        }

                        Text(
                            text = state.user ?: "",
                            style = TextStyle.dydxDefault
                                .themeFont(fontSize = ThemeFont.FontSize.medium)
                                .themeColor(foreground = ThemeColor.SemanticColor.text_primary),
                        )
                    }
                }
            }
        }
    }
}
