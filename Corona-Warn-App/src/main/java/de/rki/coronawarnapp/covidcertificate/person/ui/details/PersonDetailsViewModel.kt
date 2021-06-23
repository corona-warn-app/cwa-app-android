package de.rki.coronawarnapp.covidcertificate.person.ui.details

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CwaUserCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.PersonDetailsQrCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.RecoveryCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CertificateItem
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.TestCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationInfoCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson.Status.IMMUNITY
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transform
import timber.log.Timber

class PersonDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val personCertificatesProvider: PersonCertificatesProvider,
    private val qrCodeGenerator: QrCodeGenerator,
    private val vaccinationRepository: VaccinationRepository,
    private val timeStamper: TimeStamper,
    @Assisted private val personIdentifierCode: String,
    @Assisted private val colorShade: PersonColorShade,
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<PersonDetailsEvents>()

    private val personCertificatesFlow = personCertificatesProvider.personCertificates.mapNotNull { certificateSet ->
        certificateSet.first {
            it.personIdentifier.codeSHA256 == personIdentifierCode
        }
    }.catch { error ->
        Timber.d(error, "No person found for $personIdentifierCode")
        events.postValue(Back)
    }

    private val qrcodeCache = mutableMapOf<String, Bitmap?>()

    private val qrCodeFlow: Flow<Bitmap?> = personCertificatesFlow.transform {
        val input = it.highestPriorityCertificate.qrCode
        emit(qrcodeCache[input]) // Initial state

        val qrcode = qrcodeCache[input] ?: qrCodeGenerator.createQrCode(input, margin = 0)
        qrcodeCache[input] = qrcode
        emit(qrcode)
    }

    val uiState: LiveData<List<CertificateItem>> = combine(
        personCertificatesFlow,
        qrCodeFlow
    ) { personSpecificCertificates, qrCode ->
        assembleList(personSpecificCertificates, qrCode)
    }.asLiveData2()

    private suspend fun assembleList(personCertificates: PersonCertificates, qrCode: Bitmap?) =
        mutableListOf<CertificateItem>().apply {
            val priorityCertificate = personCertificates.highestPriorityCertificate
            add(PersonDetailsQrCard.Item(priorityCertificate, qrCode))
            add(cwaUserCard(personCertificates))

            // Find any vaccination certificate to determine the vaccination information
            personCertificates.certificates.find { it is VaccinationCertificate }?.let { certificate ->
                val vaccinatedPerson = vaccinatedPerson(certificate)
                if (vaccinatedPerson.getVaccinationStatus(timeStamper.nowUTC) != IMMUNITY) {
                    val timeUntilImmunity = vaccinatedPerson.getTimeUntilImmunity()
                    add(VaccinationInfoCard.Item(timeUntilImmunity))
                }
            }

            personCertificates.certificates.forEach { addCardItem(it, personCertificates.highestPriorityCertificate) }
        }

    private suspend fun cwaUserCard(
        personCertificates: PersonCertificates
    ) = CwaUserCard.Item(personCertificates) { checked ->
        launch {
            val identifier = if (checked) personCertificates.personIdentifier else null
            personCertificatesProvider.setCurrentCwaUser(identifier)
        }
    }

    private suspend fun MutableList<CertificateItem>.addCardItem(
        certificate: CwaCovidCertificate,
        priorityCertificate: CwaCovidCertificate
    ) {
        val isCurrentCertificate = certificate.certificateId == priorityCertificate.certificateId
        when (certificate) {
            is TestCertificate -> add(
                TestCertificateCard.Item(certificate, isCurrentCertificate, colorShade) {
                    events.postValue(OpenTestCertificateDetails(certificate.containerId))
                }
            )
            is VaccinationCertificate -> {
                val status = vaccinatedPerson(certificate).getVaccinationStatus(timeStamper.nowUTC)
                add(
                    VaccinationCertificateCard.Item(
                        certificate = certificate,
                        isCurrentCertificate = isCurrentCertificate,
                        colorShade = colorShade,
                        status = status
                    ) {
                        events.postValue(OpenVaccinationCertificateDetails(certificate.containerId))
                    }
                )
            }

            is RecoveryCertificate -> add(
                RecoveryCertificateCard.Item(certificate, isCurrentCertificate, colorShade) {
                    events.postValue(OpenRecoveryCertificateDetails(certificate.containerId))
                }
            )
        }
    }

    private suspend fun vaccinatedPerson(certificate: CwaCovidCertificate): VaccinatedPerson =
        vaccinationRepository.vaccinationInfos.first().find {
            it.identifier == certificate.personIdentifier
        }!! // Must be person

    @AssistedFactory
    interface Factory : CWAViewModelFactory<PersonDetailsViewModel> {
        fun create(
            personIdentifierCode: String,
            colorShade: PersonColorShade,
        ): PersonDetailsViewModel
    }
}
