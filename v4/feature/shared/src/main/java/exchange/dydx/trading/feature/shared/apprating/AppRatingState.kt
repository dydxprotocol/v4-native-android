package exchange.dydx.trading.feature.shared.apprating

import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class AppRatingState @Inject constructor() {
    val shouldShowDialog = false
}
