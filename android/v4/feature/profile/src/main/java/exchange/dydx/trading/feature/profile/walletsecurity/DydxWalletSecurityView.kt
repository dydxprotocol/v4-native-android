package exchange.dydx.trading.feature.profile.walletsecurity

import android.R.attr.foreground
import android.R.attr.path
import android.R.attr.shape
import android.R.attr.text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
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
fun Preview_DydxWalletSecurityView() {
    DydxThemedPreviewSurface {
        DydxWalletSecurityView.Content(Modifier, DydxWalletSecurityView.ViewState.preview)
    }
}

object DydxWalletSecurityView : DydxComponent {
    enum class LoginMethod {
        email, google, apple;

        companion object {
            fun fromString(value: String?): LoginMethod {
                return when (value?.lowercase()) {
                    "email" -> email
                    "google" -> google
                    "apple" -> apple
                    else -> email
                }
            }
        }

        fun title(localizer: LocalizerProtocol): String {
            return when (this) {
                email -> localizer.localize("APP.GENERAL.EMAIL")
                google -> "Google"
                apple -> "Apple"
            }
        }

        fun description(localizer: LocalizerProtocol): String {
            return when (this) {
                email -> localizer.localize("APP.TURNKEY_ACCOUNT.EMAIL_DESC")
                google -> localizer.localize("APP.TURNKEY_ACCOUNT.GOOGLE_DESC")
                apple -> localizer.localize("APP.TURNKEY_ACCOUNT.APPLE_DESC")
            }
        }

        val logo: Int
            get() = when (this) {
                email -> R.drawable.icon_email_2
                google -> R.drawable.icon_google
                apple -> R.drawable.icon_apple
            }
    }

    data class ViewState(
        val localizer: LocalizerProtocol,
        val loginMethod: LoginMethod = LoginMethod.email,
        val loginAction: () -> Unit = {},
        val backButtonAction: () -> Unit = {},
        val email: String?,
        val exportSourceAction: () -> Unit = {},
        val exportDydxAction: () -> Unit = {},
        val sourceAddress: String?,
        val dydxAddress: String?,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                email = "aaa@gmail.com",
                sourceAddress = "0x1234567890abcdef1234567890abcdef12345678",
                dydxAddress = "0xabcdef1234567890abcdef1234567890abcdef1234",
            )
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val viewModel: DydxWalletSecurityViewModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        Content(modifier, state)
    }

    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) {
            return
        }

        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        Column(
            modifier = modifier
                .themeColor(ThemeColor.SemanticColor.layer_2)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            HeaderView(
                title = state.localizer.localize("APP.GENERAL.ACCOUNT"),
                backAction = state.backButtonAction,
            )

            LazyColumn(
                modifier = Modifier,
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "email") {
                    EmailContent(
                        modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                        state = state,
                    )

                    Spacer(modifier = Modifier.height(ThemeShapes.VerticalPadding))
                }

                item(key = "export") {
                    ExportContent(
                        modifier = Modifier.padding(horizontal = ThemeShapes.HorizontalPadding),
                        state = state,
                    )

                    Spacer(modifier = Modifier.height(ThemeShapes.VerticalPadding))
                }
            }
        }
    }

    @Composable
    private fun EmailContent(
        modifier: Modifier,
        state: ViewState,
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TitlesContent(
                title = state.loginMethod.title(state.localizer),
                description = state.loginMethod.description(state.localizer),
            )

            val shape = RoundedCornerShape(12.dp)
            Row(
                modifier = Modifier
//                    .clickable {
//                        state.loginAction()
//                    }
                    .background(color = ThemeColor.SemanticColor.layer_3.color, shape = shape)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .clip(shape),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PlatformImage(
                    modifier = Modifier.size(24.dp),
                    icon = state.loginMethod.logo,
                )

                Text(
                    modifier = Modifier.weight(1f),
                    text = state.email ?: "",
                    style = androidx.compose.ui.text.TextStyle.dydxDefault
                        .themeColor(foreground = ThemeColor.SemanticColor.text_primary)
                        .themeFont(fontSize = ThemeFont.FontSize.medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val shape = RoundedCornerShape(percent = 50)
                Row(
                    modifier = Modifier
                        .background(color = ThemeColor.SemanticColor.layer_5.color, shape = shape)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                        .clip(shape),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    PlatformImage(
                        modifier = Modifier.size(16.dp),
                        icon = R.drawable.icon_verified,
                        colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.color_green.color),
                    )

                    Text(
                        text = state.localizer.localize(path = "APP.EMAIL_NOTIFICATIONS.VERIFIED"),
                        style = androidx.compose.ui.text.TextStyle.dydxDefault
                            .themeColor(foreground = ThemeColor.SemanticColor.color_green)
                            .themeFont(fontSize = ThemeFont.FontSize.tiny),
                    )
                }
//
//                PlatformImage(
//                    modifier = Modifier.size(12.dp),
//                    icon = R.drawable.chevron_right,
//                    colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.text_tertiary.color),
//                )
            }
        }
    }

    @Composable
    private fun ExportContent(
        modifier: Modifier,
        state: ViewState,
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TitlesContent(
                title = state.localizer.localize(path = "APP.PORTFOLIO.EXPORT"),
                description = state.localizer.localize(path = "APP.TURNKEY_ACCOUNT.EXPORT_DESC"),
            )

            ExportItemContent(
                modifier = Modifier.clickable { state.exportSourceAction() },
                title = state.localizer.localize(path = "APP.TURNKEY_ACCOUNT.EXPORT_SOURCE_WALLET"),
                address = state.sourceAddress,
                addressColor = ThemeColor.SemanticColor.text_secondary,
            )

            ExportItemContent(
                modifier = Modifier.clickable { state.exportDydxAction() },
                title = state.localizer.localize(path = "APP.TURNKEY_ACCOUNT.EXPORT_DYDX_WALLET"),
                address = state.dydxAddress,
                addressColor = ThemeColor.SemanticColor.color_purple,
            )
        }
    }

    @Composable
    private fun ExportItemContent(
        modifier: Modifier,
        title: String,
        address: String?,
        addressColor: ThemeColor.SemanticColor = ThemeColor.SemanticColor.text_secondary,
    ) {
        val shape = RoundedCornerShape(12.dp)
        Row(
            modifier = modifier
                .background(color = ThemeColor.SemanticColor.layer_3.color, shape = shape)
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .clip(shape),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                style = androidx.compose.ui.text.TextStyle.dydxDefault
                    .themeColor(foreground = ThemeColor.SemanticColor.text_secondary)
                    .themeFont(fontSize = ThemeFont.FontSize.medium),
            )

            if (address != null) {
                val shape = RoundedCornerShape(percent = 50)
                Text(
                    modifier = Modifier
                        .width(96.dp)
                        .background(color = ThemeColor.SemanticColor.layer_5.color, shape = shape)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                        .clip(shape),
                    text = address,
                    style = androidx.compose.ui.text.TextStyle.dydxDefault
                        .themeColor(foreground = addressColor)
                        .themeFont(fontSize = ThemeFont.FontSize.tiny),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            PlatformImage(
                modifier = Modifier.size(12.dp),
                icon = R.drawable.chevron_right,
                colorFilter = ColorFilter.tint(ThemeColor.SemanticColor.text_tertiary.color),
            )
        }
    }

    @Composable
    private fun TitlesContent(
        modifier: Modifier = Modifier,
        title: String,
        description: String,
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = androidx.compose.ui.text.TextStyle.dydxDefault
                    .themeColor(foreground = ThemeColor.SemanticColor.text_primary)
                    .themeFont(fontSize = ThemeFont.FontSize.medium),
            )

            Text(
                text = description,
                style = androidx.compose.ui.text.TextStyle.dydxDefault
                    .themeColor(foreground = ThemeColor.SemanticColor.text_tertiary)
                    .themeFont(fontSize = ThemeFont.FontSize.small),
            )
        }
    }
}
