package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.AdmissionState
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.MaskState
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import de.rki.coronawarnapp.covidcertificate.ScreenshotCertificateTestData
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CovidTestCertificatePendingCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificatesItem
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateWrapper
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import io.mockk.every
import io.mockk.mockk
import org.joda.time.Instant
import java.util.Locale

fun listItemWithPendingItem() = mutableListOf<PersonCertificatesItem>()
    .apply {
        add(
            CovidTestCertificatePendingCard.Item(
                certificate = mockTestCertificateWrapper(false),
                onDeleteAction = {},
                onRetryAction = {},
            )
        )

        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                            else -> "2G Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = admissionState,
                    admissionBadgeText = "2G+"
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState,
                    maskBadgeText = "Maskenpflicht"
                ),
                colorShade = PersonColorShade.COLOR_1,
                badgeCount = 0,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )
    }

fun listItemWithUpdatingItem() = mutableListOf<PersonCertificatesItem>()
    .apply {
        add(
            CovidTestCertificatePendingCard.Item(
                certificate = mockTestCertificateWrapper(true),
                onDeleteAction = {},
                onRetryAction = {},
            )
        )

        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                            else -> "2G Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = null,
                    admissionBadgeText = "2G+"
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState.copy(),
                    maskBadgeText = "Maskenpflicht"
                ),
                colorShade = PersonColorShade.COLOR_1,
                badgeCount = 0,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )
    }

fun gStatusIsNullAndMaskIsVisibleItem() = mutableListOf<PersonCertificatesItem>()
    .apply {
        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                            else -> "2G Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = null,
                    admissionBadgeText = ""
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState,
                    maskBadgeText = "Maskenpflicht"
                ),
                colorShade = PersonColorShade.COLOR_1,
                badgeCount = 0,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )
    }

fun gStatusIsNullAndMaskIsNullItem() = mutableListOf<PersonCertificatesItem>()
    .apply {
        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                            else -> "2G Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = null,
                    admissionBadgeText = ""
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = null,
                    maskBadgeText = ""
                ),
                colorShade = PersonColorShade.COLOR_1,
                badgeCount = 0,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )
    }

fun gStatusIsNullAndMaskIsInvisibleItem() = mutableListOf<PersonCertificatesItem>()
    .apply {

        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                            else -> "2G Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = null,
                    admissionBadgeText = ""
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState.copy(visible = false),
                    maskBadgeText = "Maskenpflicht"
                ),
                colorShade = PersonColorShade.COLOR_1,
                badgeCount = 0,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )
    }

fun gStatusIsInvisibleAndMaskIsVisibleItem() = mutableListOf<PersonCertificatesItem>()
    .apply {
        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                            else -> "2G Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = admissionState.copy(visible = false),
                    admissionBadgeText = "2G"
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState,
                    maskBadgeText = "Maskenpflicht"
                ),
                colorShade = PersonColorShade.COLOR_1,
                badgeCount = 0,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )
    }

fun gStatusIsInvisibleAndMaskIsInvisibleItem() = mutableListOf<PersonCertificatesItem>()
    .apply {
        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                            else -> "2G Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = admissionState.copy(visible = false),
                    admissionBadgeText = "2G"
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState.copy(visible = false),
                    maskBadgeText = "Maskenpflicht"
                ),
                colorShade = PersonColorShade.COLOR_1,
                badgeCount = 0,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )
    }

fun gStatusIsVisibleAndMaskIsInvisibleItem() = mutableListOf<PersonCertificatesItem>()
    .apply {
        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                            else -> "2G Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = admissionState.copy(visible = true),
                    admissionBadgeText = "2G"
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState.copy(visible = false),
                    maskBadgeText = "Maskenpflicht"
                ),
                colorShade = PersonColorShade.COLOR_1,
                badgeCount = 0,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )
    }

fun gStatusIsVisibleAndMaskIsVisibleItem() = mutableListOf<PersonCertificatesItem>()
    .apply {
        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                            else -> "2G Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = admissionState,
                    admissionBadgeText = "2G"
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState,
                    maskBadgeText = "Maskenpflicht"
                ),
                colorShade = PersonColorShade.COLOR_1,
                badgeCount = 0,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )
    }

fun maskFree() = mutableListOf<PersonCertificatesItem>().apply {
    add(
        PersonCertificateCard.Item(
            overviewCertificates = listOf(
                PersonCertificateCard.Item.OverviewCertificate(
                    mockVaccinationCertificate("Andrea Schneider")
                )
            ),
            admission = PersonCertificateCard.Item.Admission(
                state = admissionState,
                admissionBadgeText = "2G"
            ),
            mask = PersonCertificateCard.Item.Mask(
                state = maskState.copy(identifier = MaskState.MaskStateIdentifier.OPTIONAL),
                maskBadgeText = "Keine Maskenpflicht"
            ),
            colorShade = PersonColorShade.GREEN,
            badgeCount = 0,
            onClickAction = { _, _ -> },
            onCovPassInfoAction = {},
            onCertificateSelected = {},
        )
    )
}

fun maskFreeMultiLine() = mutableListOf<PersonCertificatesItem>().apply {
    add(
        PersonCertificateCard.Item(
            overviewCertificates = listOf(
                PersonCertificateCard.Item.OverviewCertificate(
                    mockVaccinationCertificate("Andrea Schneider")
                )
            ),
            admission = PersonCertificateCard.Item.Admission(
                state = admissionState,
                admissionBadgeText = "2G"
            ),
            mask = PersonCertificateCard.Item.Mask(
                state = maskState,
                maskBadgeText = "Keine Maskenpflicht, Multi Line, aber mindestens zwei"
            ),
            colorShade = PersonColorShade.GREEN,
            badgeCount = 0,
            onClickAction = { _, _ -> },
            onCovPassInfoAction = {},
            onCertificateSelected = {},
        )
    )
}

fun maskRequiredAndNoStatus() = mutableListOf<PersonCertificatesItem>().apply {
    add(
        PersonCertificateCard.Item(
            overviewCertificates = listOf(
                PersonCertificateCard.Item.OverviewCertificate(
                    mockVaccinationCertificate("Andrea Schneider")
                )
            ),
            admission = PersonCertificateCard.Item.Admission(
                state = null,
                admissionBadgeText = "2G+"
            ),
            mask = PersonCertificateCard.Item.Mask(
                state = maskState.copy(identifier = MaskState.MaskStateIdentifier.REQUIRED),
                maskBadgeText = "Maskenpflicht"
            ),
            colorShade = PersonColorShade.COLOR_3,
            badgeCount = 0,
            onClickAction = { _, _ -> },
            onCovPassInfoAction = {},
            onCertificateSelected = {},
        )
    )
}

fun maskInvalidOutdated() = mutableListOf<PersonCertificatesItem>().apply {
    add(
        PersonCertificateCard.Item(
            overviewCertificates = listOf(
                PersonCertificateCard.Item.OverviewCertificate(
                    mockInvalidVaccinationCertificate("Andrea Schneider")
                )
            ),
            admission = PersonCertificateCard.Item.Admission(
                state = null,
                admissionBadgeText = "2G+"
            ),
            mask = PersonCertificateCard.Item.Mask(
                state = maskState.copy(),
                maskBadgeText = "Maskenpflicht"
            ),
            colorShade = PersonColorShade.COLOR_INVALID,
            badgeCount = 0,
            onClickAction = { _, _ -> },
            onCovPassInfoAction = {},
            onCertificateSelected = {},
        )
    )
}

fun noMaskInfoStatusInfo() = mutableListOf<PersonCertificatesItem>().apply {
    add(
        PersonCertificateCard.Item(
            overviewCertificates = listOf(
                PersonCertificateCard.Item.OverviewCertificate(
                    mockVaccinationCertificate("Andrea Schneider")
                )
            ),
            admission = PersonCertificateCard.Item.Admission(
                state = admissionState,
                admissionBadgeText = "2G+"
            ),
            mask = PersonCertificateCard.Item.Mask(
                state = maskState.copy(visible = false),
                maskBadgeText = ""
            ),
            colorShade = PersonColorShade.COLOR_1,
            badgeCount = 0,
            onClickAction = { _, _ -> },
            onCovPassInfoAction = {},
            onCertificateSelected = {},
        )
    )
}

fun noMaskInfoNoStatusInfo() = mutableListOf<PersonCertificatesItem>().apply {
    add(
        PersonCertificateCard.Item(
            overviewCertificates = listOf(
                PersonCertificateCard.Item.OverviewCertificate(
                    mockVaccinationCertificate("Andrea Schneider")
                )
            ),
            admission = PersonCertificateCard.Item.Admission(
                state = null,
                admissionBadgeText = ""
            ),
            mask = PersonCertificateCard.Item.Mask(
                state = maskState.copy(visible = false),
                maskBadgeText = ""
            ),
            colorShade = PersonColorShade.COLOR_3,
            badgeCount = 0,
            onClickAction = { _, _ -> },
            onCovPassInfoAction = {},
            onCertificateSelected = {},
        )
    )
}

fun personsItems() = mutableListOf<PersonCertificatesItem>()
    .apply {
        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockTestCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "Testzertifikat"
                            else -> "Test Certificate"
                        }
                    ),
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = admissionState,
                    admissionBadgeText = "3G"
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState.copy(visible = false),
                    maskBadgeText = ""
                ),
                colorShade = PersonColorShade.COLOR_1,
                badgeCount = 5,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )

        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockTestCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "Testzertifikat"
                            else -> "Test Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = admissionState,
                    admissionBadgeText = "3G"
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState.copy(visible = false),
                    maskBadgeText = ""
                ),
                colorShade = PersonColorShade.COLOR_2,
                badgeCount = 3,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )

        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                            else -> "2G Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = null,
                    admissionBadgeText = "2G+"
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState.copy(),
                    maskBadgeText = "Maskenpflicht"
                ),
                colorShade = PersonColorShade.COLOR_3,
                badgeCount = 0,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )
    }

fun onePersonItem() = mutableListOf<PersonCertificatesItem>()
    .apply {
        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                            else -> "2G Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = null,
                    admissionBadgeText = "2G+"
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState.copy(),
                    maskBadgeText = "Maskenpflicht"
                ),
                colorShade = PersonColorShade.COLOR_1,
                badgeCount = 0,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )
    }

fun onePersonItemWithBadgeCount() = mutableListOf<PersonCertificatesItem>()
    .apply {
        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                            else -> "2G Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = admissionState,
                    admissionBadgeText = "2G"
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState.copy(visible = false),
                    maskBadgeText = ""
                ),
                colorShade = PersonColorShade.COLOR_1,
                badgeCount = 1,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )
    }

fun twoGPlusCertificate() = mutableListOf<PersonCertificatesItem>()
    .apply {
        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                            else -> "2G Certificate"
                        }
                    ),
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockTestCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "Testzertifikat"
                            else -> "Test Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = admissionState,
                    admissionBadgeText = "2G+"
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState.copy(visible = false),
                    maskBadgeText = ""
                ),
                colorShade = PersonColorShade.COLOR_1,
                badgeCount = 0,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )
    }

fun threeCertificates() = mutableListOf<PersonCertificatesItem>()
    .apply {
        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "Geimpft"
                            else -> "2G Certificate"
                        }
                    ),
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockTestCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "Getestet"
                            else -> "Test Certificate"
                        }
                    ),
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "Genesen"
                            else -> "Recovery Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = admissionState,
                    admissionBadgeText = "2G+"
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState.copy(visible = false),
                    maskBadgeText = ""
                ),
                colorShade = PersonColorShade.COLOR_1,
                badgeCount = 0,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )
    }

fun twoGPlusCertificateWithBadge() = mutableListOf<PersonCertificatesItem>()
    .apply {
        add(
            PersonCertificateCard.Item(
                overviewCertificates = listOf(
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockVaccinationCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "2G-Zertifikat"
                            else -> "2G Certificate"
                        }
                    ),
                    PersonCertificateCard.Item.OverviewCertificate(
                        mockTestCertificate("Andrea Schneider"),
                        buttonText = when (Locale.getDefault()) {
                            Locale.GERMANY, Locale.GERMAN -> "Testzertifikat"
                            else -> "Test Certificate"
                        }
                    )
                ),
                admission = PersonCertificateCard.Item.Admission(
                    state = admissionState,
                    admissionBadgeText = "2G+"
                ),
                mask = PersonCertificateCard.Item.Mask(
                    state = maskState.copy(visible = false),
                    maskBadgeText = ""
                ),
                colorShade = PersonColorShade.COLOR_1,
                badgeCount = 1,
                onClickAction = { _, _ -> },
                onCovPassInfoAction = {},
                onCertificateSelected = {},
            )
        )
    }

val maskState = MaskState(
    visible = true,
    badgeText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Befreiung"),
        parameters = listOf()
    ),
    titleText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Masken-Befreiung"),
        parameters = listOf()
    ),
    subtitleText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Für Sie gilt keine Maskenpflicht"),
        parameters = listOf()
    ),
    longText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Aufgrund Ihrer Zertifikate [...]"),
        parameters = listOf()
    ),
    faqAnchor = "dcc_admission_state",
    identifier = MaskState.MaskStateIdentifier.OPTIONAL
)

private val admissionState = AdmissionState(
    visible = true,
    badgeText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "2G+"),
        parameters = listOf()
    ),
    titleText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Status-Nachweis"),
        parameters = listOf()
    ),
    subtitleText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "2G+ PCR-Test"),
        parameters = listOf()
    ),
    longText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Ihre Zertifikate erfüllen [...]"),
        parameters = listOf()
    ),
    stateChangeNotificationText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Ihre Zertifikate erfüllen derzeit keine Regel."),
        parameters = listOf()
    ),
    identifier = "admission_state_identifier",
    faqAnchor = "dcc_admission_state"
)

private fun mockTestCertificate(
    name: String,
    isPending: Boolean = false,
    isUpdating: Boolean = false
): TestCertificate = mockk<TestCertificate>().apply {
    every { headerExpiresAt } returns Instant.now().plus(20)
    every { isCertificateRetrievalPending } returns isPending
    every { isUpdatingData } returns isUpdating
    every { fullName } returns name
    every { registeredAt } returns Instant.parse("2021-05-21T11:35:00.000Z")
    every { personIdentifier } returns CertificatePersonIdentifier(
        firstNameStandardized = "firstNameStandardized",
        lastNameStandardized = "lastNameStandardized",
        dateOfBirthFormatted = "1943-04-18"
    )
    every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.testCertificate)
    every { isDisplayValid } returns true
    every { sampleCollectedAt } returns Instant.parse("2021-05-21T11:35:00.000Z")
    every { state } returns CwaCovidCertificate.State.Valid(headerExpiresAt)
    every { isNew } returns false
}

private fun mockTestCertificateWrapper(isUpdating: Boolean) = mockk<TestCertificateWrapper>().apply {
    every { isCertificateRetrievalPending } returns true
    every { isUpdatingData } returns isUpdating
    every { registeredAt } returns Instant.EPOCH
    every { containerId } returns TestCertificateContainerId("testCertificateContainerId")
}

private fun mockVaccinationCertificate(name: String): VaccinationCertificate =
    mockk<VaccinationCertificate>().apply {
        every { headerExpiresAt } returns Instant.now().plus(20)
        every { containerId } returns VaccinationCertificateContainerId("2")
        val localDate = Instant.parse("2021-06-01T11:35:00.000Z").toLocalDateUserTz()
        every { fullName } returns name
        every { fullNameFormatted } returns name
        every { doseNumber } returns 2
        every { totalSeriesOfDoses } returns 2
        every { vaccinatedOn } returns localDate.minusDays(15)
        every { personIdentifier } returns CertificatePersonIdentifier(
            firstNameStandardized = "firstNameStandardized",
            lastNameStandardized = "lastNameStandardized",
            dateOfBirthFormatted = "1943-04-18"
        )
        every { isDisplayValid } returns true
        every { state } returns CwaCovidCertificate.State.Valid(headerExpiresAt)
        every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.vaccinationCertificate)
    }

private fun mockInvalidVaccinationCertificate(name: String): VaccinationCertificate =
    mockk<VaccinationCertificate>().apply {
        every { headerExpiresAt } returns Instant.now().plus(20)
        every { containerId } returns VaccinationCertificateContainerId("2")
        val localDate = Instant.parse("2019-06-01T11:35:00.000Z").toLocalDateUserTz()
        every { fullName } returns name
        every { fullNameFormatted } returns name
        every { doseNumber } returns 2
        every { totalSeriesOfDoses } returns 2
        every { vaccinatedOn } returns localDate.minusDays(15)
        every { personIdentifier } returns CertificatePersonIdentifier(
            firstNameStandardized = "firstNameStandardized",
            lastNameStandardized = "lastNameStandardized",
            dateOfBirthFormatted = "1943-04-18"
        )
        every { isDisplayValid } returns false
        every { state } returns CwaCovidCertificate.State.Invalid()
        every { qrCodeToDisplay } returns CoilQrCode(ScreenshotCertificateTestData.vaccinationCertificate)
    }
