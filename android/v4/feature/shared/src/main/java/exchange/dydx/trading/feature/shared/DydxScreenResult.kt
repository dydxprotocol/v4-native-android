package exchange.dydx.trading.feature.shared

import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.dydxstatemanager.AbacusStateManagerProtocol
import exchange.dydx.platformui.components.container.PlatformInfo
import exchange.dydx.platformui.components.container.PlatformInfoViewModel

enum class DydxScreenResult {
    NoRestriction,
    UserRestriction,
    SourceRestriction,
    DestinationRestriction,
    GeoRestriction,
    UnknownRestriction;

    companion object {
        fun from(restriction: Restriction): DydxScreenResult {
            return when (restriction) {
                Restriction.NO_RESTRICTION -> NoRestriction
                Restriction.USER_RESTRICTED -> UserRestriction
                Restriction.USER_RESTRICTION_UNKNOWN -> UnknownRestriction
                Restriction.GEO_RESTRICTED -> GeoRestriction
            }
        }
    }

    fun showRestrictionAlert(
        toaster: PlatformInfo,
        localizer: LocalizerProtocol,
        abacusStateManager: AbacusStateManagerProtocol,
        buttonAction: (() -> Unit)? = null,
    ) {
        when (this) {
            DydxScreenResult.NoRestriction -> {
            }
            DydxScreenResult.UserRestriction -> {
                toaster.show(
                    title = localizer.localize("ERRORS.ONBOARDING.WALLET_RESTRICTED_ERROR_TITLE"),
                    message = localizer.localize("ERRORS.ONBOARDING.REGION_NOT_PERMITTED_SUBTITLE"),
                    type = PlatformInfoViewModel.Type.Error,
                    buttonTitle = localizer.localize("APP.GENERAL.OK"),
                    buttonAction = {
                        buttonAction?.invoke()
                        abacusStateManager.replaceCurrentWallet()
                    },
                    duration = PlatformInfoViewModel.Duration.Indefinite,
                )
            }
            DydxScreenResult.SourceRestriction -> {
                toaster.show(
                    title = localizer.localize("ERRORS.ONBOARDING.WALLET_RESTRICTED_ERROR_TITLE"),
                    message = localizer.localize("ERRORS.ONBOARDING.WALLET_RESTRICTED_WITHDRAWAL_TRANSFER_ORIGINATION_ERROR_MESSAGE"),
                    type = PlatformInfoViewModel.Type.Error,
                    buttonTitle = localizer.localize("APP.GENERAL.OK"),
                    buttonAction = buttonAction,
                    duration = PlatformInfoViewModel.Duration.Indefinite,
                )
            }
            DydxScreenResult.DestinationRestriction -> {
                toaster.show(
                    title = localizer.localize("ERRORS.ONBOARDING.WALLET_RESTRICTED_ERROR_TITLE"),
                    message = localizer.localize("ERRORS.ONBOARDING.WALLET_RESTRICTED_WITHDRAWAL_TRANSFER_DESTINATION_ERROR_MESSAGE"),
                    type = PlatformInfoViewModel.Type.Error,
                    buttonTitle = localizer.localize("APP.GENERAL.OK"),
                    buttonAction = buttonAction,
                    duration = PlatformInfoViewModel.Duration.Indefinite,
                )
            }
            DydxScreenResult.GeoRestriction -> {
                toaster.show(
                    title = localizer.localize("ERRORS.ONBOARDING.REGION_NOT_PERMITTED_TITLE"),
                    message = localizer.localize("ERRORS.ONBOARDING.REGION_NOT_PERMITTED_SUBTITLE"),
                    type = PlatformInfoViewModel.Type.Error,
                    buttonTitle = null, // localizer.localize("APP.GENERAL.OK"),
                    buttonAction = {
                        buttonAction?.invoke()
                        // exit app
                        // android.os.Process.killProcess(android.os.Process.myPid());
                    },
                    duration = PlatformInfoViewModel.Duration.Indefinite,
                    cancellable = false,
                )
            }
            DydxScreenResult.UnknownRestriction -> {
                toaster.show(
                    title = localizer.localize("ERRORS.GENERAL.RATE_LIMIT_REACHED_ERROR_TITLE"),
                    message = localizer.localize("ERRORS.GENERAL.RATE_LIMIT_REACHED_ERROR_MESSAGE"),
                    type = PlatformInfoViewModel.Type.Error,
                    buttonTitle = localizer.localize("APP.GENERAL.OK"),
                    buttonAction = buttonAction,
                    duration = PlatformInfoViewModel.Duration.Indefinite,
                )
            }
        }
    }
}
