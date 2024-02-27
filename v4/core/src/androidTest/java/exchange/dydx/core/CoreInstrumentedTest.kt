package exchange.dydx.core

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.components.composesupport.config.withComposeSupport
import exchange.dydx.trading.TradingTestActivity
import exchange.dydx.trading.TradingTestActivity.C
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 *
 * Imported from:
 * https://github.com/KasperskyLab/Kaspresso/tree/master/samples/kaspresso-compose-support-sample
 */
@RunWith(AndroidJUnit4::class)
class CoreInstrumentedTest : com.kaspersky.kaspresso.testcases.api.testcase.TestCase(
    kaspressoBuilder = com.kaspersky.kaspresso.kaspresso.Kaspresso.Builder.withComposeSupport(
        customize = {
            flakySafetyParams = com.kaspersky.kaspresso.params.FlakySafetyParams.Companion.custom(
                timeoutMs = 5000,
                intervalMs = 1000,
            )
        },
        lateComposeCustomize = { composeBuilder ->
            composeBuilder.semanticsBehaviorInterceptors = composeBuilder.semanticsBehaviorInterceptors.filter {
                it !is com.kaspersky.components.composesupport.interceptors.behavior.impl.systemsafety.SystemDialogSafetySemanticsBehaviorInterceptor
            }.toMutableList()
        },
    ),
) {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<TradingTestActivity>()

    @Test
    fun test() = run {
        step("Open Flaky screen") {
            io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen<ComposeMainScreen>(
                composeTestRule,
            ) {
                button {
                    performClick()
                }
            }
        }
    }
}

private class ComposeMainScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<ComposeMainScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag(C.Tag.main_screen_container) },
    ) {

    val button: KNode = child {
        hasTestTag(C.Tag.main_screen_button)
    }
}
