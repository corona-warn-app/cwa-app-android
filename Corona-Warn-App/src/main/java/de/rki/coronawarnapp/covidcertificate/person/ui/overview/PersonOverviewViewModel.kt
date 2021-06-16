package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transform
import timber.log.Timber

class PersonOverviewViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    certificatesProvider: PersonCertificatesProvider,
    private val testCertificateRepository: TestCertificateRepository,
    private val qrCodeGenerator: QrCodeGenerator,
) : CWAViewModel(dispatcherProvider) {

    private val qrCodes = mutableMapOf<String, Bitmap?>()
    val events = SingleLiveEvent<PersonOverviewFragmentEvents>()
    val personCertificates: LiveData<List<CertificatesItem>> = combine(
        certificatesProvider.personCertificates,
        certificatesProvider.qrCodesFlow
    ) { persons, qrCodesMap ->
        mapPersons(persons, qrCodesMap)
    }.asLiveData(dispatcherProvider.Default)

    private fun mapPersons(persons: Set<PersonCertificates>, qrCodesMap: Map<String, Bitmap?>): List<CertificatesItem> =
        mutableListOf<CertificatesItem>().apply {
            addPendingCards(persons)
            addCertificateCards(persons, qrCodesMap)
        }

    private fun MutableList<CertificatesItem>.addCertificateCards(
        persons: Set<PersonCertificates>,
        qrCodes: Map<String, Bitmap?>
    ) {
        persons.filterNotPending()
            .forEachIndexed { index, person ->
                val certificate = person.highestPriorityCertificate
                add(
                    PersonCertificateCard.Item(
                        certificate = certificate,
                        qrcodeBitmap = qrCodes[certificate.qrCode],
                        color = PersonOverviewItemColor.colorFor(index),
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
                        certificate = certificate,
                        onRetryAction = { refreshCertificate(certificate.certificateId) },
                        onDeleteAction = { events.postValue(ShowDeleteDialog(certificate.certificateId)) }
                    )
                )
            }
        }
    }

    private val PersonCertificatesProvider.qrCodesFlow
        get() = personCertificates
            .transform { persons ->
                emit(emptyMap()) // Initial state
                persons.filterNotPending()
                    .forEach {
                        val qrCode = it.highestPriorityCertificate.qrCode
                        qrCodes[qrCode] = generateQrCode(qrCode)
                        emit(qrCodes)
                    }
            }

    private fun Set<PersonCertificates>.filterNotPending() = this
        .filter { !it.hasPendingTestCertificate() }
        .sortedBy { it.highestPriorityCertificate.fullName }
        .sortedByDescending { it.isCwaUser }

    private suspend fun generateQrCode(qrCode: QrCodeString): Bitmap? = try {
        qrCodeGenerator.createQrCode(qrCode, margin = 0)
    } catch (e: Exception) {
        Timber.d(e, "generateQrCode failed for $qrCode")
        null
    }

    private fun refreshCertificate(identifier: TestCertificateIdentifier) =
        launch {
            val error = testCertificateRepository.refresh(identifier).mapNotNull { it.error }.singleOrNull()
            error?.let { events.postValue(ShowRefreshErrorDialog(error)) }
        }

    fun deleteTestCertificate(identifier: TestCertificateIdentifier) = launch {
        testCertificateRepository.deleteCertificate(identifier)
    }

    fun onScanQrCode() {
        events.postValue(ScanQrCode)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<PersonOverviewViewModel>
}
