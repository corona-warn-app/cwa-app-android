package de.rki.coronawarnapp.vaccination.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.vaccination.ui.details.VaccinationDetailsFragment
import de.rki.coronawarnapp.vaccination.ui.details.VaccinationDetailsFragmentModule
import de.rki.coronawarnapp.vaccination.ui.list.VaccinationListFragment
import de.rki.coronawarnapp.vaccination.ui.list.VaccinationListFragmentModule

@Module
abstract class VaccinationUIModule {

    @ContributesAndroidInjector(modules = [VaccinationListFragmentModule::class])
    abstract fun vaccinationListFragment(): VaccinationListFragment

    @ContributesAndroidInjector(modules = [VaccinationDetailsFragmentModule::class])
    abstract fun vaccinationDetailsFragment(): VaccinationDetailsFragment
}
