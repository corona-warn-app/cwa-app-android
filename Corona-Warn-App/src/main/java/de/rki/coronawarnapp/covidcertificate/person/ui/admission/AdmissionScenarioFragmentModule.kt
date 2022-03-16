package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class AdmissionScenarioFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(AdmissionScenariosViewModel::class)
    abstract fun admissionScenariosFragment(
        factory: AdmissionScenariosViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
