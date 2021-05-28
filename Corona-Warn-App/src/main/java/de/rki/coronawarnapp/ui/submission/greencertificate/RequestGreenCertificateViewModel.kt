package de.rki.coronawarnapp.ui.submission.greencertificate

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import org.joda.time.LocalDate

class RequestGreenCertificateViewModel @AssistedInject constructor(
    @Assisted private val testType: CoronaTest.Type,
) : CWAViewModel() {

    fun birthDateChanged(localDate: LocalDate?) {
        // TODO
    }

    fun onAgreeGC() {
        // TODO
    }

    fun onDisagreeGC() {
        // TODO
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<RequestGreenCertificateViewModel> {
        fun create(type: CoronaTest.Type): RequestGreenCertificateViewModel
    }
}
