package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DccTicketingCertificateSelectionFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(DccTicketingCertificateSelectionViewModel::class)
    abstract fun dccTicketingCertificateSelectionFragment(
        factory: DccTicketingCertificateSelectionViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
