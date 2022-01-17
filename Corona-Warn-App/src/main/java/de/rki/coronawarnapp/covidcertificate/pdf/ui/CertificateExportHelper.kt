package de.rki.coronawarnapp.covidcertificate.pdf.ui

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry

/**
 * Certificate could be exported only if DCC country is Germany (DE)
 */
fun CwaCovidCertificate.canBeExported() = headerIssuer == DccCountry.DE
