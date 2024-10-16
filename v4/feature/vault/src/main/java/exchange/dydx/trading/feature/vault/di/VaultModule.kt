package exchange.dydx.trading.feature.vault.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.abacus.functional.vault.VaultHistoryEntry
import exchange.dydx.trading.feature.vault.components.ChartType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Module
@InstallIn(ActivityRetainedComponent::class)
interface VaultModule {

    @Binds
    fun bindVaultSelectedHistoryEntryFlow(
        mutableFlow: MutableStateFlow<VaultHistoryEntry?>,
    ): Flow<VaultHistoryEntry?>

    @Binds
    fun bindVaultHistoryEntryFlow(
        mutableFlow: MutableStateFlow<List<VaultHistoryEntry>?>,
    ): Flow<List<VaultHistoryEntry>?>

    @Binds
    fun bindVaultChartTypeFlow(
        mutableFlow: MutableStateFlow<ChartType?>,
    ): Flow<ChartType?>

    companion object {
        @Provides
        @ActivityRetainedScoped
        fun provideMutableSelectedVaultHistoryEntryFlow(): MutableStateFlow<VaultHistoryEntry?> {
            return MutableStateFlow(null)
        }

        @Provides
        @ActivityRetainedScoped
        fun provideMutableVaultHistoryFlow(): MutableStateFlow<List<VaultHistoryEntry>?> {
            return MutableStateFlow(null)
        }

        @Provides
        @ActivityRetainedScoped
        fun provideMutableVaultChartTypeFlow(): MutableStateFlow<ChartType?> {
            return MutableStateFlow(null)
        }
    }
}
