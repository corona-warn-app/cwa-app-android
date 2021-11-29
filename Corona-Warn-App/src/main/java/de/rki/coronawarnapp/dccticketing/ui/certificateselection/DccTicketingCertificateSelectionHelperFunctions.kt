package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType.PCR_TEST
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType.RA_TEST
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType.RECOVERY
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType.TEST
import de.rki.coronawarnapp.dccticketing.core.certificateselection.DccTicketingCertificatesFilterType.VACCINATION

fun Context.certificateTypesText(certificateTypes: List<String>, separator: String = ", "): String =
    certificateTypes.joinToString(separator) { type ->
        when (DccTicketingCertificatesFilterType.typeOf(type)) {
            VACCINATION -> getString(R.string.vaccination_certificate_name)
            RECOVERY -> getString(R.string.recovery_certificate_name)
            PCR_TEST -> getString(R.string.pcr_test_certificate)
            RA_TEST -> getString(R.string.rat_test_certificate)
            TEST -> listOf(
                getString(R.string.rat_test_certificate),
                getString(R.string.pcr_test_certificate)
            ).joinToString(separator)
            else -> ""
        }
    }

fun getFullName(familyName: String?, givenName: String?): String =
    listOfNotNull(familyName, givenName).joinToString("<<")
