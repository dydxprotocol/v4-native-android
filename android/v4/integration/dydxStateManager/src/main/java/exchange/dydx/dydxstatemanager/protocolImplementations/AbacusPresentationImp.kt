package exchange.dydx.dydxstatemanager.protocolImplementations

import exchange.dydx.abacus.protocols.PresentationProtocol
import exchange.dydx.abacus.protocols.Toast
import exchange.dydx.abacus.protocols.ToastType
import exchange.dydx.platformui.components.container.PlatformInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbacusPresentationImp @Inject constructor(
    private val toaster: PlatformInfo
) : PresentationProtocol {
    override fun showToast(toast: Toast) {
        toaster.show(
            title = toast.title,
            message = toast.text ?: "",
            type = when (toast.type) {
                ToastType.Info -> exchange.dydx.platformui.components.container.PlatformInfoViewModel.Type.Info
                ToastType.Warning -> exchange.dydx.platformui.components.container.PlatformInfoViewModel.Type.Warning
                ToastType.Error -> exchange.dydx.platformui.components.container.PlatformInfoViewModel.Type.Error
            },
        )
    }
}
