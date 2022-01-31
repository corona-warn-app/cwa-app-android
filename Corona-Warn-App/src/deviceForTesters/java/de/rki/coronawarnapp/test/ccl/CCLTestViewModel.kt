package de.rki.coronawarnapp.test.ccl

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.DccWalletInfoCalculationManager
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.dummyDccWalletInfo
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
    private val dccWalletInfoRepository: DccWalletInfoRepository,
    vaccinationRepository: VaccinationRepository,
    testCertificateRepository: TestCertificateRepository,
    recoveryCertificateRepository: RecoveryCertificateRepository,
    private val dccWalletInfoRepository: DccWalletInfoRepository,
    private val dccWalletInfoCalculationManager: DccWalletInfoCalculationManager
) : CWAViewModel() {
    var selectedPersonIdentifier: PersonIdentifierSelection = PersonIdentifierSelection.Random
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
            .map { PersonIdentifierSelection.Selected(it) } + PersonIdentifierSelection.Random
    }.asLiveData2()

    fun addDccWallet() = launch {
        dccWalletInfoRepository.save(selectedPersonIdentifier.getCertificatePersonIdentifier(), dummyDccWalletInfo)
    }

    fun clearDccWallet() = launch {
        dccWalletInfoRepository.clear()
    }

    sealed class PersonIdentifierSelection {
        data class Selected(val personIdentifier: CertificatePersonIdentifier) : PersonIdentifierSelection()
        object Random : PersonIdentifierSelection()

        fun getCertificatePersonIdentifier() = when (this) {
            Random -> CertificatePersonIdentifier(
                firstNameStandardized = firstNames.random(),
                lastNameStandardized = lastNames.random(),
                dateOfBirthFormatted = birthDates.random()
            )
            is Selected -> personIdentifier
        }

        companion object {
            private val firstNames = listOf("Aa", "Bb", "Cc", "Dd", "Rr", "Ff", "Xx", "Hh")
            private val lastNames = listOf("Jj", "Kk", "Ll", "Vv", "Qq", "Pp", "Oo", "Ss")
            private val birthDates = listOf(
                "2020-10-10", "2021-10-10", "2020-12-10", "2020-11-10",
                "2020-09-10", "2021-08-10", "2020-01-10", "2020-02-10"
            )
        }
    }

    fun triggerCalculation() = launch {
        dccWalletInfoCalculationManager.triggerCalculation()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CCLTestViewModel>
}
