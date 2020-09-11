package de.rki.coronawarnapp.ui.main

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.TestRiskLevelCalculation
import de.rki.coronawarnapp.ui.test.TestRiskLevelCalculationFragmentModule

@Module
abstract class MainActivityTestModule {

     @ContributesAndroidInjector(modules = [TestRiskLevelCalculationFragmentModule::class])
     abstract fun testRiskLevelCalculationFragment(): TestRiskLevelCalculation
}
