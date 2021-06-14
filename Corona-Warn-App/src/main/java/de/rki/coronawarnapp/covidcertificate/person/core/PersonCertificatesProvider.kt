package de.rki.coronawarnapp.covidcertificate.person.core

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Instant
import org.joda.time.LocalDate
import javax.inject.Inject

// Aggregate the certificates and sort them
@Reusable
class PersonCertificatesProvider @Inject constructor(
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
    private val testCertificateRepository: TestCertificateRepository,
    private val vaccinationRepository: VaccinationRepository,
) {

    // TODO remove
    fun testCertificate(index: Int, isCertificateRetrievalPending: Boolean = false, isUpdating: Boolean = false) =
        object : TestCertificate {
            override val targetName: String
                get() = "targetName"
            override val testType: String
                get() = "testType"
            override val testResult: String
                get() = "testResult"
            override val testName: String?
                get() = "testName"
            override val testNameAndManufacturer: String?
                get() = "testNameAndManufacturer"
            override val sampleCollectedAt: Instant
                get() = Instant.now()
            override val testResultAt: Instant?
                get() = Instant.now()
            override val testCenter: String
                get() = "testCenter"
            override val registeredAt: Instant
                get() = Instant.now()
            override val isUpdatingData: Boolean
                get() = isUpdating
            override val isCertificateRetrievalPending: Boolean
                get() = isCertificateRetrievalPending
            override val issuer: String
                get() = "issuer"
            override val issuedAt: Instant
                get() = Instant.now()
            override val expiresAt: Instant
                get() = Instant.now()
            override val qrCode: QrCodeString
                get() = "qrCode"
            override val firstName: String?
                get() = "Max"
            override val lastName: String
                get() = "Mustermann"
            override val fullName: String
                get() = "Max Mustermann"
            override val dateOfBirth: LocalDate
                get() = LocalDate.now()
            override val personIdentifier: CertificatePersonIdentifier
                get() = CertificatePersonIdentifier(
                    LocalDate.now(),
                    "last$index",
                    "firs$index"
                )
            override val certificateIssuer: String
                get() = "certificateIssuer"
            override val certificateCountry: String
                get() = "certificateCountry"
            override val certificateId: String
                get() = "certificateId"
        }

    // TODO
    val personCertificates: Flow<Set<PersonCertificates>> = flowOf(
        setOf(
            PersonCertificates(
                listOf(
                    testCertificate(0),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(1),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(2),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(3),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(4),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(5),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(6),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(7, true, true),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(8),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(9),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(10),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(11, true, false),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(12),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(13),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(14),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(15),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(16),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(17),
                )
            ),
            PersonCertificates(
                listOf(
                    testCertificate(18),
                )
            )
        )
    )
}
