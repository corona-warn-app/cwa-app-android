package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CameraPermissionCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificatesItem
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CovidTestCertificatePendingCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateWrapper
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.permission.CameraPermissionProvider
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import timber.log.Timber

class PersonOverviewViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    certificatesProvider: PersonCertificatesProvider,
    private val testCertificateRepository: TestCertificateRepository,
    private val qrCodeGenerator: QrCodeGenerator,
    valueSetsRepository: ValueSetsRepository,
    @AppContext context: Context,
    private val cameraPermissionProvider: CameraPermissionProvider,
) : CWAViewModel(dispatcherProvider) {

    init {
        valueSetsRepository.triggerUpdateValueSet(languageCode = context.getLocale())
    }

    private val qrCodes = mutableMapOf<String, Bitmap?>()
    val events = SingleLiveEvent<PersonOverviewFragmentEvents>()
    val personCertificates: LiveData<List<PersonCertificatesItem>> = combine(
        cameraPermissionProvider.deniedPermanently,
        certificatesProvider.personCertificates,
        testCertificateRepository.certificates,
        certificatesProvider.qrCodesFlow
    ) { denied, persons, tcWrappers, qrCodesMap ->
        mutableListOf<PersonCertificatesItem>().apply {
            if (denied) add(CameraPermissionCard.Item { events.postValue(OpenAppDeviceSettings) })
            addPersonItems(persons, tcWrappers, qrCodesMap)
        }
    }.asLiveData(dispatcherProvider.Default)

    val markNewCertsAsSeen = testCertificateRepository.certificates
        .onEach { wrappers ->
            wrappers
                .filter { !it.seenByUser && !it.isCertificateRetrievalPending }
                .forEach {
                    testCertificateRepository.markCertificateAsSeenByUser(it.containerId)
                }
        }
        .map { }
        .catch { Timber.w("Failed to mark certificates as seen.") }
        .asLiveData2()

    fun deleteTestCertificate(containerId: TestCertificateContainerId) = launch {
        testCertificateRepository.deleteCertificate(containerId)
    }

    fun onScanQrCode() = events.postValue(ScanQrCode)

    fun checkCameraSettings() = cameraPermissionProvider.checkSettings()

    private fun MutableList<PersonCertificatesItem>.addPersonItems(
        persons: Set<PersonCertificates>,
        tcWrappers: Set<TestCertificateWrapper>,
        qrCodesMap: Map<String, Bitmap?>
    ) {
        addPendingCards(tcWrappers)
        addCertificateCards(persons, qrCodesMap)
    }

    private fun MutableList<PersonCertificatesItem>.addCertificateCards(
        persons: Set<PersonCertificates>,
        qrCodes: Map<String, Bitmap?>
    ) {
        persons.filterNotPending()
            .forEachIndexed { index, person ->
                val certificate = person.highestPriorityCertificate
                val color = PersonColorShade.shadeFor(index)
                add(
                    PersonCertificateCard.Item(
                        certificate = certificate,
                        qrcodeBitmap = qrCodes[certificate.qrCode],
                        colorShade = color
                    ) { _, position ->
                        events.postValue(OpenPersonDetailsFragment(person.personIdentifier.codeSHA256, position, color))
                    }
                )
            }
    }

    private fun MutableList<PersonCertificatesItem>.addPendingCards(tcWrappers: Set<TestCertificateWrapper>) {
        tcWrappers.filter {
            it.isCertificateRetrievalPending
        }.forEach { certificateWrapper ->
            add(
                CovidTestCertificatePendingCard.Item(
                    certificate = certificateWrapper,
                    onRetryAction = { refreshCertificate(certificateWrapper.containerId) },
                    onDeleteAction = { events.postValue(ShowDeleteDialog(certificateWrapper.containerId)) }
                )
            )
        }
    }

    private fun PersonCertificates.hasPendingTestCertificate(): Boolean {
        val certificate = highestPriorityCertificate
        return certificate is TestCertificate && certificate.isCertificateRetrievalPending
    }

    private val PersonCertificatesProvider.qrCodesFlow
        get() = personCertificates.transform { persons ->
            emit(qrCodes) // Initial state
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

    fun refreshCertificate(containerId: TestCertificateContainerId) =
        launch {
            val error = testCertificateRepository.refresh(containerId).mapNotNull { it.error }.singleOrNull()
            error?.let { events.postValue(ShowRefreshErrorDialog(error)) }
        }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<PersonOverviewViewModel>
}
