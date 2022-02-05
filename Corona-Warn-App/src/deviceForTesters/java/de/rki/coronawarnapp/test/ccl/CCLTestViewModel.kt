package de.rki.coronawarnapp.test.ccl

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.DccWalletInfoCalculationManager
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine

class CCLTestViewModel @AssistedInject constructor(
    vaccinationRepository: VaccinationRepository,
    testCertificateRepository: TestCertificateRepository,
    recoveryCertificateRepository: RecoveryCertificateRepository,
    private val dccWalletInfoRepository: DccWalletInfoRepository,
    private val dccWalletInfoCalculationManager: DccWalletInfoCalculationManager
) : CWAViewModel() {
    var selectedPersonIdentifier: PersonIdentifierSelection = PersonIdentifierSelection.All
    val dccWalletInfoList = dccWalletInfoRepository.personWallets.asLiveData2()
    val personIdentifiers = combine(
        vaccinationRepository.vaccinationInfos,
        testCertificateRepository.cwaCertificates,
        recoveryCertificateRepository.cwaCertificates
    ) { vaccinatedPersons, tests, recoveries ->
        val vaccinations = vaccinatedPersons.flatMap { it.vaccinationCertificates }.toSet()
        val allCerts: Set<CwaCovidCertificate> = (vaccinations + tests + recoveries)
        allCerts.map { it.personIdentifier }
            .distinct()
            .map { PersonIdentifierSelection.Selected(it) }
            .toMutableList<PersonIdentifierSelection>()
            .apply { add(0, PersonIdentifierSelection.All) }
            .toList()

    }.asLiveData2()

    fun clearDccWallet() = launch {
        dccWalletInfoRepository.clear()
    }

    sealed class PersonIdentifierSelection {
        data class Selected(val personIdentifier: CertificatePersonIdentifier) : PersonIdentifierSelection()
        object All : PersonIdentifierSelection()

        fun getCertificatePersonIdentifier() = when (this) {
            is Selected -> personIdentifier
            else -> null
        }
    }

    fun triggerCalculation() = launch {
        selectedPersonIdentifier.getCertificatePersonIdentifier()?.let {
            dccWalletInfoCalculationManager.triggerCalculationForPerson(it)
        } ?: run {
            dccWalletInfoCalculationManager.triggerCalculation()
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CCLTestViewModel>
}
