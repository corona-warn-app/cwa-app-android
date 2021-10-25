package de.rki.coronawarnapp.reyclebin.ui

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTest

sealed class RecyclerBinEvent {

    object ConfirmRemoveAll : RecyclerBinEvent()

    data class RemoveCertificate(val item: CwaCovidCertificate, val position: Int?) : RecyclerBinEvent()

    data class RemoveTest(val item: RecycledCoronaTest, val position: Int?) : RecyclerBinEvent()

    data class ConfirmRestoreCertificate(val item: CwaCovidCertificate) : RecyclerBinEvent()

    data class ConfirmRestoreTest(val item: RecycledCoronaTest) : RecyclerBinEvent()
}
