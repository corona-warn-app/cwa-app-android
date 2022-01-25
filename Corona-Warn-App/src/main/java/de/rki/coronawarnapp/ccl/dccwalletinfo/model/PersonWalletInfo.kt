package de.rki.coronawarnapp.ccl.dccwalletinfo.model


data class PersonWalletInfo(
    val personIdentifier: String,
    val dccWalletInfo: DccWalletInfo
)
