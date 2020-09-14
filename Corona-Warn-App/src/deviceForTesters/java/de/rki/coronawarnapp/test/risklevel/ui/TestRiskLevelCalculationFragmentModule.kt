package de.rki.coronawarnapp.test.risklevel.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.VDC
import de.rki.coronawarnapp.util.viewmodel.VDCFactory
import de.rki.coronawarnapp.util.viewmodel.VDCKey

@Module
abstract class TestRiskLevelCalculationFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(TestRiskLevelCalculationFragmentVDC::class)
    abstract fun testRiskLevelFragment(factory: TestRiskLevelCalculationFragmentVDC.Factory): VDCFactory<out VDC>
}

