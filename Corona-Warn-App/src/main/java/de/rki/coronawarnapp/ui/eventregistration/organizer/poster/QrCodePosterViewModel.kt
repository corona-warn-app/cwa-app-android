package de.rki.coronawarnapp.ui.eventregistration.organizer.poster

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class QrCodePosterViewModel @AssistedInject constructor(
    private val dispatcher: DispatcherProvider,
    private val qrCodeGenerator: QrCodeGenerator,
) : CWAViewModel()
{


    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<QrCodePosterViewModel>
}
