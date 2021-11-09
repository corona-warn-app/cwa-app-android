package de.rki.coronawarnapp.test.dccticketing

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DccTicketingTestModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(DccTicketingTestViewModel::class)
    abstract fun dscTest(factory: DccTicketingTestViewModel.Factory): CWAViewModelFactory<out CWAViewModel>
}
