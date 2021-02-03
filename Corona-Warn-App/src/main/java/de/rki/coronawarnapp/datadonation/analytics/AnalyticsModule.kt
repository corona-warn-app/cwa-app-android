package de.rki.coronawarnapp.datadonation.analytics

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows.NewExposureWindowsDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.KeySubmissionStateDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest.RegisteredTestDonor
import de.rki.coronawarnapp.datadonation.analytics.modules.risklevelhistory.RiskLevelHistoryChangesDonor

@Module
class AnalyticsModule {

    @IntoSet
    @Provides
    fun newExposureWindows(module: NewExposureWindowsDonor): DonorModule = module

    @IntoSet
    @Provides
    fun keySubmission(module: KeySubmissionStateDonor): DonorModule = module

    @IntoSet
    @Provides
    fun registeredTest(module: RegisteredTestDonor): DonorModule = module

    @IntoSet
    @Provides
    fun riskLevelHistory(module: RiskLevelHistoryChangesDonor): DonorModule = module
}
