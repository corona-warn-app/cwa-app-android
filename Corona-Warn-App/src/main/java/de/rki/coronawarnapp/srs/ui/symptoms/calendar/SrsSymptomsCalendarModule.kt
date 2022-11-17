package de.rki.coronawarnapp.srs.ui.symptoms.calendar

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SrsSymptomsCalendarModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(SrsSymptomsCalendarViewModel::class)
    abstract fun srsSymptomsCalendarViewModel(
        factory: SrsSymptomsCalendarViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
