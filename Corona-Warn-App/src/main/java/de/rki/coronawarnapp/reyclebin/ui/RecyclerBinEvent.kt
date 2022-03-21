package de.rki.coronawarnapp.reyclebin.ui

import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.reyclebin.coronatest.request.RestoreRecycledTestRequest

sealed class RecyclerBinEvent {

    object ConfirmRemoveAll : RecyclerBinEvent()

    data class RemoveCertificate(val certificate: CwaCovidCertificate, val position: Int?) : RecyclerBinEvent()

    data class RemoveTest(val test: CoronaTest, val position: Int?) : RecyclerBinEvent()

    data class ConfirmRestoreCertificate(val certificate: CwaCovidCertificate) : RecyclerBinEvent()

    data class ConfirmRestoreTest(val test: PersonalCoronaTest) : RecyclerBinEvent()

    data class RestoreDuplicateTest(val restoreRecycledTestRequest: RestoreRecycledTestRequest) : RecyclerBinEvent()
}
