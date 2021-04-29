package de.rki.coronawarnapp.vaccination.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class VaccinationUIModule {
    @ContributesAndroidInjector(modules = [VaccinationTestFragmentModule::class])
    abstract fun vaccinationTestFragment(): VaccinationTestFragment
}
