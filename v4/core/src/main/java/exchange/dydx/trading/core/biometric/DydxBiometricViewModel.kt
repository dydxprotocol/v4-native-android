package exchange.dydx.trading.core.biometric

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.trading.common.DydxViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

data class BiometricError(
    val text: String? = null,
)

@HiltViewModel
class DydxBiometricViewModel @Inject constructor(
    private val localizer: LocalizerProtocol,
) : ViewModel(), DydxViewModel {

    val state: Flow<DydxBiometricView.ViewState?> = flowOf(createViewState())

    private fun createViewState(): DydxBiometricView.ViewState {
        return DydxBiometricView.ViewState(
            localizer = localizer,
        )
    }
}
