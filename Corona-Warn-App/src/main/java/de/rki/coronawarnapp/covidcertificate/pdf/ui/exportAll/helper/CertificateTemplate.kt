package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll.helper

import android.content.Context
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.util.di.AppContext
import java.lang.UnsupportedOperationException
import javax.inject.Inject

@Reusable
class CertificateTemplate @Inject constructor(
    @AppContext private val context: Context
) {
    private val templates = mutableMapOf<String, String>()
    operator fun invoke(cwaCovidCertificate: CwaCovidCertificate): String =
        when (cwaCovidCertificate.headerIssuer) {
            DccCountry.DE -> templateDE(cwaCovidCertificate)
            else -> template(cwaCovidCertificate)
        }

    private fun templateDE(cwaCovidCertificate: CwaCovidCertificate): String = when (cwaCovidCertificate) {
        is RecoveryCertificate -> templates[RC_DE] ?: read("de_rc_v4.1.svg").also { templates[RC_DE] = it }
        is TestCertificate -> templates[TC_DE] ?: read("de_tc_v4.1.svg").also { templates[TC_DE] = it }
        is VaccinationCertificate -> templates[VC_DE] ?: read("de_vc_v4.1.svg").also { templates[VC_DE] = it }
        else -> throw UnsupportedOperationException("${cwaCovidCertificate::class.simpleName} isn't supported")
    }

    private fun template(cwaCovidCertificate: CwaCovidCertificate): String = when (cwaCovidCertificate) {
        is RecoveryCertificate -> templates[RC] ?: read("rc_v4.1.svg").also { templates[RC] = it }
        is TestCertificate -> templates[TC] ?: read("tc_v4.1.svg").also { templates[TC] = it }
        is VaccinationCertificate -> templates[VC] ?: read("vc_v4.1.svg").also { templates[VC] = it }
        else -> throw UnsupportedOperationException("${cwaCovidCertificate::class.simpleName} isn't supported")
    }

    private fun read(file: String): String =
        context.assets.open("template/$file").bufferedReader().use {
            it.readText()
        }

    companion object {
        private const val RC_DE = "RC_DE"
        private const val TC_DE = "TC_DE"
        private const val VC_DE = "VC_DE"

        private const val RC = "RC"
        private const val TC = "TC"
        private const val VC = "VC"
    }
}
