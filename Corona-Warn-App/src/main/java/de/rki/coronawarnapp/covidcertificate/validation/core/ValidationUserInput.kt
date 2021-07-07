package de.rki.coronawarnapp.covidcertificate.validation.core

import android.os.Parcelable
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import kotlinx.parcelize.Parcelize
import org.joda.time.Instant

@Parcelize
data class ValidationUserInput(
    val arrivalCountry: DccCountry,
    val arrivalAt: Instant,
) : Parcelable
