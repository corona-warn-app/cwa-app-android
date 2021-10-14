package de.rki.coronawarnapp.reyclebin.ui

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate

sealed class RecyclerBinEvent {

    object ConfirmRemoveAll : RecyclerBinEvent()

    data class ConfirmRemoveItem(val item: CwaCovidCertificate, val position: Int?) : RecyclerBinEvent()

    data class ConfirmRestoreItem(val item: CwaCovidCertificate) : RecyclerBinEvent()
}
