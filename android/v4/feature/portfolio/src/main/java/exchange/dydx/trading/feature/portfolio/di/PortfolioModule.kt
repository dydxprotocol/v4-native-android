package exchange.dydx.trading.feature.portfolio.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import exchange.dydx.trading.feature.portfolio.DydxPortfolioView
import exchange.dydx.trading.feature.portfolio.components.overview.DydxPortfolioSectionsView
import exchange.dydx.trading.feature.portfolio.components.placeholder.DydxPortfolioPlaceholderView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Module
@InstallIn(ActivityRetainedComponent::class)
interface PortfolioModule {

    @Binds
    fun bindDisplayContent(
        mutableFlow: MutableStateFlow<DydxPortfolioView.DisplayContent>,
    ): Flow<DydxPortfolioView.DisplayContent>

    @Binds
    fun bindSections(
        mutableFlow: MutableStateFlow<DydxPortfolioSectionsView.Selection>,
    ): Flow<DydxPortfolioSectionsView.Selection>

    @Binds
    fun bindPlacholderSections(
        mutableFlow: MutableStateFlow<DydxPortfolioPlaceholderView.Selection>,
    ): Flow<DydxPortfolioPlaceholderView.Selection>

    companion object {
        @Provides
        @ActivityRetainedScoped
        fun provideMutableDisplayContent(): MutableStateFlow<DydxPortfolioView.DisplayContent> {
            return MutableStateFlow(DydxPortfolioView.DisplayContent.Overview)
        }

        @Provides
        @ActivityRetainedScoped
        fun providateMutableSections(): MutableStateFlow<DydxPortfolioSectionsView.Selection> {
            return MutableStateFlow(DydxPortfolioSectionsView.Selection.Positions)
        }

        @Provides
        @ActivityRetainedScoped
        fun providateMutablePlaceholderSections(): MutableStateFlow<DydxPortfolioPlaceholderView.Selection> {
            return MutableStateFlow(DydxPortfolioPlaceholderView.Selection.Positions)
        }
    }
}
