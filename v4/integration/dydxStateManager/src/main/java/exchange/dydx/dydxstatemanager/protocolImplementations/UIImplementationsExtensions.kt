package exchange.dydx.dydxstatemanager.protocolImplementations

import exchange.dydx.abacus.state.app.helper.DynamicLocalizer
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.UIImplementations
import exchange.dydx.dydxstatemanager.BuildConfig
import exchange.dydx.trading.common.formatter.DydxFormatter
import java.util.Locale

object UIImplementationsExtensions {
    var shared: UIImplementations? = null

    fun reset(language: String?, ioImplementations: IOImplementations) {
        val systemLanguage = language ?: Locale.getDefault().language
        val loadLocalOnly = BuildConfig.DEBUG

        val localizer = shared?.localizer ?: DynamicLocalizer(
            ioImplementations = ioImplementations,
            systemLanguage = systemLanguage ?: "en",
            path = "/config",
            endpoint = "https://dydx-v4-shared-resources.vercel.app/config",
            loadLocalOnly = loadLocalOnly,
        )

        val formatter = shared?.formatter ?: AbacusFormatterImp(DydxFormatter())
        shared = UIImplementations(localizer = localizer, formatter = formatter)
    }
}
