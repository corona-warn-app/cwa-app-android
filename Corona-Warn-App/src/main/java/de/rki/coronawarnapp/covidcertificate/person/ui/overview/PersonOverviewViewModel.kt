package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateIdentifier
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CovidTestCertificatePendingCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CertificatesItem
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map

class PersonOverviewViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    certificatesProvider: PersonCertificatesProvider,
    private val testCertificateRepository: TestCertificateRepository,
    private val qrCodeGenerator: QrCodeGenerator,
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<PersonOverviewFragmentEvents>()
    val personCertificates: LiveData<List<CertificatesItem>> = certificatesProvider
        .personCertificates.map {
            mapPersons(it)
        }
        .asLiveData(dispatcherProvider.Default)

    private fun mapPersons(persons: Set<PersonCertificates>): List<CertificatesItem> =
        mutableListOf<CertificatesItem>().apply {
            addPendingCards(persons)
            addCertificateCards(persons, mapOf()) // TODO generate qr codes
        }

    private fun MutableList<CertificatesItem>.addCertificateCards(
        persons: Set<PersonCertificates>,
        qrCodes: Map<String, Bitmap>
    ) {
        persons
            .filter { !it.hasPendingTestCertificate() }
            .sortedBy { it.isCwaUser }
            .forEach { person ->
                val certificate = person.highestPriorityCertificate
                add(
                    PersonCertificateCard.Item(
                        certificate = certificate,
                        qrcodeBitmap = qrCodes[certificate.qrCode],
                        onClickAction = {
                            events.postValue(
                                OpenPersonDetailsFragment(person.personIdentifier.codeSHA256)
                            )
                        }
                    )
                )
            }
    }

    private fun PersonCertificates.hasPendingTestCertificate(): Boolean {
        val certificate = highestPriorityCertificate
        return certificate is TestCertificate && certificate.isCertificateRetrievalPending
    }

    private fun MutableList<CertificatesItem>.addPendingCards(persons: Set<PersonCertificates>) {
        persons.forEach {
            val certificate = it.highestPriorityCertificate
            if (certificate is TestCertificate && certificate.isCertificateRetrievalPending) {
                add(
                    CovidTestCertificatePendingCard.Item(
                        testDate = certificate.registeredAt,
                        isUpdatingData = certificate.isUpdatingData,
                        onRetryAction = { refreshCertificate(certificate.certificateId) },
                        onDeleteAction = { events.postValue(ShowDeleteDialog(certificate.certificateId)) }
                    )
                )
            }
        }
    }

    private fun refreshCertificate(identifier: TestCertificateIdentifier) =
        launch {
            val error = testCertificateRepository.refresh(identifier).mapNotNull { it.error }.singleOrNull()
            error?.let { events.postValue(ShowRefreshErrorDialog(error)) }
        }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<PersonOverviewViewModel>
}
