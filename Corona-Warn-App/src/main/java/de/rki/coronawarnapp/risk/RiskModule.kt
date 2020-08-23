package de.rki.coronawarnapp.risk

import dagger.Binds
import dagger.Module

@Module
abstract class RiskModule {

    @Binds
    abstract fun bindRiskLevelCalculation(
        riskLevelCalculation: RiskLevelCalculationImpl
    ): RiskLevelCalculation

    @Binds
    abstract fun bindRiskScoreAnalysis(
        riskScoreAnalysis: RiskScoreAnalysisImpl
    ): RiskScoreAnalysis
}