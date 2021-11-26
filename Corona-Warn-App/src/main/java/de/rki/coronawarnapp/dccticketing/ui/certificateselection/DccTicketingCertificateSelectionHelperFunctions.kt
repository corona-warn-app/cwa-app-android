package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType.PCR_TEST
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType.RA_TEST
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType.RECOVERY
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType.TEST
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType.VACCINATION

fun requestedCertificateTypes(certificateTypes: List<String>, context: Context, separator: String): String =
    certificateTypes.joinToString(separator) { certificate ->
        when (certificate) {
            VACCINATION.type -> context.getString(R.string.vaccination_certificate_name)
            RECOVERY.type -> context.getString(R.string.recovery_certificate_name)
            PCR_TEST.type,
            TEST.type,
            RA_TEST.type -> context.getString(R.string.test_certificate_name)
            else -> ""
        }
    }

fun getFullName(familyName: String?, givenName: String?): String =
    listOf(familyName, givenName).joinToString("<<")
