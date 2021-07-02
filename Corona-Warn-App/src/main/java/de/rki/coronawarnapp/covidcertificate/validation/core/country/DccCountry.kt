package de.rki.coronawarnapp.covidcertificate.validation.core.country

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
data class DccCountry(
    // ISO 3166 two-letter country-codes
    val countryCode: String,
) : Parcelable {

    /**
     * User readable country name, returns country code as fallback.
     */
    fun getCountryDisplayName(userLocale: Locale = Locale.getDefault()): String {
        return Locale(userLocale.language, countryCode.uppercase()).getDisplayCountry(userLocale)
    }
}
