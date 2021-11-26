package de.rki.coronawarnapp.dccticketing.ui.certificateselection

enum class DccTicketingCertificatesFilterType(val type: String) {
    VACCINATION("v"),
    RECOVERY("r"),
    TEST("t"),
    PCR_TEST("tp"),
    RA_TEST("tr");

    companion object {
        fun typeOf(type: String): DccTicketingCertificatesFilterType? = values()
            .find { it.type == type }
    }
}
