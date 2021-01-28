package de.rki.coronawarnapp.datadonation.storage

import de.rki.coronawarnapp.datadonation.OneTimePassword
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OTPRepository @Inject constructor(
    private val dataDonationPreferences: DataDonationPreferences
) {

    val lastOTP: OneTimePassword?
        get() = dataDonationPreferences.oneTimePassword.value

    fun generateOTP(): OneTimePassword = OneTimePassword().also {
        dataDonationPreferences.oneTimePassword.update { it }
    }

    fun clear() {
        dataDonationPreferences.oneTimePassword.update { null }
    }
}
