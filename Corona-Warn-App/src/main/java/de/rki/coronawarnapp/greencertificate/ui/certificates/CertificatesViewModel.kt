package de.rki.coronawarnapp.greencertificate.ui.certificates

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class CertificatesViewModel @AssistedInject constructor() : CWAViewModel() {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CertificatesViewModel>
}
