package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import android.content.Context
import de.rki.coronawarnapp.R

fun getRequestedCertificateTypes(requestedCertificatesList: List<String>, context: Context, separator: String): String =
    requestedCertificatesList.joinToString(",") { certificate ->
        when (certificate) {
            FilterType.VACCINATION.type -> context.getString(R.string.vaccination_certificate_name)
            FilterType.RECOVERY.type -> context.getString(R.string.recovery_certificate_name)
            FilterType.PCR_TEST.type,
            FilterType.TEST.type,
            FilterType.RA_TEST.type -> context.getString(R.string.test_certificate_name)
            else -> ""
        }
    }

fun getFullName(familyName: String?, givenName: String?): String =
    listOf(familyName, givenName).joinToString("<<")

private enum class FilterType(val type: String) {
    VACCINATION("v"),
    RECOVERY("r"),
    TEST("t"),
    PCR_TEST("tp"),
    RA_TEST("tr");
}
