package exchange.dydx.trading.core.biometric

import androidx.biometric.BiometricManager
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.localizeWithParams
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.utilities.utils.Logging

object DydxBiometricPrompt {

    private const val TAG = "BiometricPrompt"

    data class ViewState(
        val localizer: LocalizerProtocol,
    ) {
        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
            )
        }
    }

    @Composable
    fun Content(
        activity: FragmentActivity,
        logger: Logging,
        processSuccess: (Boolean, String?) -> Unit,
    ) {
        val viewModel: DydxBiometricPromptModel = hiltViewModel()

        val state = viewModel.state.collectAsStateWithLifecycle(initialValue = null).value
        val localizer = state?.localizer ?: return

        val status = biometricStatus(activity = activity)
        when (status) {
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                processSuccess(true, null)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                processSuccess(false, "BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED")
                return
            }
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Fall through
            }
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)
                logger.d(TAG, "errCode is $errCode and errString is: $errString")
                val error = localizer.localizeWithParams(
                    path = "ERRORS.GENERAL.SOMETHING_WENT_WRONG_WITH_MESSAGE",
                    params = mapOf("ERROR_MESSAGE" to errString.toString()),
                )
                processSuccess(false, error)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                logger.d(TAG, "User biometric rejected.")
                processSuccess(false, localizer.localize("APP.GENERAL.AUTHENTICATE_TO_PROCEED"))
            }

            override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                logger.d(TAG, "Authentication was successful")
                processSuccess(true, null)
            }
        }

        val prompt = androidx.biometric.BiometricPrompt(activity, executor, callback)

        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(localizer.localize("APP.GENERAL.AUTHENTICATE_WITH_BIOMETRICS"))
            setSubtitle(localizer.localize("APP.GENERAL.AUTHENTICATE_TO_PROCEED"))
            setNegativeButtonText(localizer.localize("APP.GENERAL.CANCEL"))
            setConfirmationRequired(true)
            setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        }.build()

        prompt.authenticate(promptInfo)
    }

    private fun biometricStatus(activity: FragmentActivity): Int {
        val biometricManager = androidx.biometric.BiometricManager.from(activity)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
    }
}
