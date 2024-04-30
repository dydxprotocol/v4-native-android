package exchange.dydx.dydxstatemanager.protocolImplementations

import exchange.dydx.abacus.protocols.PresentationProtocol
import exchange.dydx.abacus.protocols.Toast
import exchange.dydx.abacus.protocols.ToastType
import exchange.dydx.platformui.components.PlatformInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbacusPresentationImp @Inject constructor(
    private val platformInfo: PlatformInfo
) : PresentationProtocol {
    override fun showToast(toast: Toast) {
        platformInfo.show(
            title = toast.title,
            message = toast.text ?: "",
            type = when (toast.type) {
                ToastType.Info -> PlatformInfo.InfoType.Info
                ToastType.Warning -> PlatformInfo.InfoType.Warning
                ToastType.Error -> PlatformInfo.InfoType.Error
            },
        )
    }
}
