package de.rki.coronawarnapp.risk

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent


@Module
@InstallIn(ApplicationComponent::class)
abstract class RiskModule {

    @Binds
    abstract fun bindRiskLevelCalculation(
        riskLevelCalculationImpl: RiskLevelCalculationImpl
    ): RiskLevelCalculation

    @Binds
    abstract fun bindRiskScoreValidation(
        riskScoreValidationImpl: RiskScoreAnalysisImpl
    ): RiskScoreAnalysis
}