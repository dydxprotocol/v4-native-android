package exchange.dydx.trading

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.components.composesupport.interceptors.behavior.impl.systemsafety.SystemDialogSafetySemanticsBehaviorInterceptor
import com.kaspersky.kaspresso.kaspresso.Kaspresso.Builder
import com.kaspersky.kaspresso.params.FlakySafetyParams
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import exchange.dydx.trading.common.ViewTags
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
class AppInstrumentedTests : TestCase(
    kaspressoBuilder = Builder.withComposeSupport(
        customize = {
            flakySafetyParams = FlakySafetyParams.custom(timeoutMs = 5000, intervalMs = 1000)
        },
        lateComposeCustomize = { composeBuilder ->
            composeBuilder.semanticsBehaviorInterceptors = composeBuilder.semanticsBehaviorInterceptors.filter {
                it !is SystemDialogSafetySemanticsBehaviorInterceptor
            }.toMutableList()
        },
    ),
) {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<TradingActivity>()

    @Test
    fun test() = run {
        step("Open Compose screen") {
            ComposeScreen.onComposeScreen<ComposeMainScreen>(
                composeTestRule,
            ) {
                contentRoot {
                    assertIsDisplayed()
                }
            }
        }

        step("Press action button") {
            ComposeScreen.onComposeScreen<ComposeMainScreen>(
                composeTestRule,
            ) {
                floatingActionButton { performClick() }
            }
        }
    }
}

private class ComposeMainScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<ComposeMainScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag(ViewTags.scaffold_root) },
    ) {

    val contentRoot: KNode = child {
        hasTestTag(ViewTags.content_root)
    }

    val floatingActionButton: KNode = child {
        hasTestTag(ViewTags.floating_action_button)
    }
}
