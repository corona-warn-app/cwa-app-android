package de.rki.coronawarnapp.test.ccl

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.dummyDccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class CCLTestViewModel @AssistedInject constructor(
    private val dccWalletInfoRepository: DccWalletInfoRepository
) : CWAViewModel() {

    private val firstNames = listOf("Aa", "Bb", "Cc", "Dd", "Rr", "Ff", "Xx", "Hh")
    private val lastNames = listOf("Jj", "Kk", "Ll", "Vv", "Qq", "Pp", "Oo", "Ss")
    private val birthDates = listOf(
        "2020-10-10", "2021-10-10", "2020-12-10", "2020-11-10",
        "2020-09-10", "2021-08-10", "2020-01-10", "2020-02-10"
    )

    val dccWalletInfoList = dccWalletInfoRepository.personWallets.asLiveData2()

    fun addDccWallet() = launch {
        val personIdentifier = CertificatePersonIdentifier(
            firstNameStandardized = firstNames.random(),
            lastNameStandardized = lastNames.random(),
            dateOfBirthFormatted = birthDates.random()
        )
        dccWalletInfoRepository.save(personIdentifier, dummyDccWalletInfo)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CCLTestViewModel>
}
