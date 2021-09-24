package de.rki.coronawarnapp.ui.coronatest.rat.profile.qrcode

import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode

sealed class ProfileQrCodeNavigation {
    object Back : ProfileQrCodeNavigation()
    object OpenScanner : ProfileQrCodeNavigation()
    data class FullQrCode(val qrCode: CoilQrCode) : ProfileQrCodeNavigation()
}
