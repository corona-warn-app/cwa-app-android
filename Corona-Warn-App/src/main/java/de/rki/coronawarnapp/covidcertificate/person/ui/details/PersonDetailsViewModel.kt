package de.rki.coronawarnapp.covidcertificate.person.ui.details

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CwaUserCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.PersonDetailsQrCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.SpecificCertificatesItem
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transform

class PersonDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    certificatesProvider: PersonCertificatesProvider,
    private val qrCodeGenerator: QrCodeGenerator,
    @Assisted private val personIdentifierCode: String
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<PersonDetailsEvents>()

    private val personSpecificCertificatesFlow = certificatesProvider.personCertificates.mapNotNull { certificateSet ->
        certificateSet
            .firstOrNull { it.personIdentifier.codeSHA256 == personIdentifierCode }
    }

    private val qrCodeFlow: Flow<Bitmap?> = personSpecificCertificatesFlow.transform {
        emit(null)
        emit(qrCodeGenerator.createQrCode(it.highestPriorityCertificate.qrCode, margin = 0))
    }

    val uiState: LiveData<List<SpecificCertificatesItem>> = combine(
        personSpecificCertificatesFlow,
        qrCodeFlow
    ) { personSpecificCertificates, qrCode ->
        assembleList(personSpecificCertificates, qrCode)
    }.asLiveData()

    private fun assembleList(certificatesList: PersonCertificates, qrCode: Bitmap?) =
        mutableListOf<SpecificCertificatesItem>().apply {
            val highestPriorityCertificate = certificatesList.highestPriorityCertificate
            add(
                PersonDetailsQrCard.Item(
                    certificate = highestPriorityCertificate,
                    qrCodeBitmap = qrCode
                )
            )
            add(
                CwaUserCard.Item(
                    certificate = highestPriorityCertificate,
                    fullName = highestPriorityCertificate.fullName,
                    dateOfBirth = highestPriorityCertificate.dateOfBirth
                )
            )
            for (certificate in certificatesList.certificates) {
                when (certificate) {
                    is TestCertificate -> {
                        // TODO add test certificate specific cards here
                    }
                    is VaccinationCertificate -> {
                        // TODO add vaccination certificate specific cards here
                    }
                    is RecoveryCertificate -> {
                        // TODO add recovery certificate specific cards here
                    }
                }
            }
        }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<PersonDetailsViewModel> {
        fun create(
            personIdentifierCode: String
        ): PersonDetailsViewModel
    }
}
