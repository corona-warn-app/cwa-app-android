package de.rki.coronawarnapp.ui.presencetracing.organizer.qrinfo

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class TraceLocationQRInfoFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(TraceLocationQRInfoViewModel::class)
    abstract fun traceLocationCreateViewModel(factory: TraceLocationQRInfoViewModel.Factory):
        CWAViewModelFactory<out CWAViewModel>
}
