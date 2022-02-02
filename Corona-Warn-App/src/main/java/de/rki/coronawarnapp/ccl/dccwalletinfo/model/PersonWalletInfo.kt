package de.rki.coronawarnapp.ccl.dccwalletinfo.model

data class PersonWalletInfo(
    val personGroupKey: String,
    val dccWalletInfo: DccWalletInfo?
)
