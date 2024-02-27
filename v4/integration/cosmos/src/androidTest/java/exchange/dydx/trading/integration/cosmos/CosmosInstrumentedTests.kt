package exchange.dydx.trading.integration.cosmos

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.components.composesupport.interceptors.behavior.impl.systemsafety.SystemDialogSafetySemanticsBehaviorInterceptor
import com.kaspersky.kaspresso.kaspresso.Kaspresso.Builder
import com.kaspersky.kaspresso.params.FlakySafetyParams
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
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
class CosmosInstrumentedTests : TestCase(
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
)
