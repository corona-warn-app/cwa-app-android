package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.ALREADY_REGISTERED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccValidityMeasures
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccValidityMeasuresObserver
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureList
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationMigration
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationStorage
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runTest2
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

class VaccinationCertificateRepositoryTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper

    @MockK lateinit var storage: VaccinationStorage
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var vaccinationValueSet: VaccinationValueSets
    @MockK lateinit var dccStateChecker: DccStateChecker
    @MockK lateinit var vaccinationMigration: VaccinationMigration
    @MockK lateinit var dccValidityMeasuresObserver: DccValidityMeasuresObserver

    private var testStorage: Set<VaccinatedPersonData> = emptySet()

    @Inject lateinit var vaccinationTestData: VaccinationTestData
    @Inject lateinit var dccQrCodeExtractor: DccQrCodeExtractor

    // Few days after issued dates of person A in test data.
    private var nowUTC = Instant.parse("2021-05-13T09:25:00.000Z")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        DaggerCovidCertificateTestComponent.factory().create().inject(this)

        coEvery {
            dccStateChecker(
                any(),
                any(),
                any()
            )
        } returns CwaCovidCertificate.State.Invalid()

        every { timeStamper.nowUTC } returns nowUTC

        every { valueSetsRepository.latestVaccinationValueSets } returns flowOf(vaccinationValueSet)

        storage.apply {
            coEvery { loadLegacyData() } answers { testStorage }
            coEvery { save(any()) } answers { testStorage = arg(0) }
            coEvery { load() } returns setOf()
            coEvery { clearLegacyData() } just Runs
        }
        every { dccValidityMeasuresObserver.dccValidityMeasures } returns flowOf(
            DccValidityMeasures(
                dscSignatureList = DscSignatureList(listOf(), Instant.EPOCH),
                revocationList = listOf(),
                blockedQrCodeHashes = setOf()
            )
        )

        vaccinationMigration = VaccinationMigration(storage)
    }

    private fun createInstance(scope: CoroutineScope) = VaccinationCertificateRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        timeStamper = timeStamper,
        storage = storage,
        valueSetsRepository = valueSetsRepository,
        qrCodeExtractor = dccQrCodeExtractor,
        dccState = dccStateChecker,
        vaccinationMigration = vaccinationMigration,
        dccValidityMeasuresObserver = dccValidityMeasuresObserver
    )

    @Test
    fun `add new certificate - no prior data`() = runTest2 {
        val instance = createInstance(this)
        instance.registerCertificate(VaccinationTestData.personAVac1QRCode).apply {
            Timber.i("Returned cert is %s", this)
            this.personIdentifier shouldBe
                CertificatePersonIdentifier(
                    dateOfBirthFormatted = "1966-11-11",
                    lastNameStandardized = "ASTRA<EINS",
                    firstNameStandardized = "ANDREAS"
                )
        }
        coVerify { storage.save(any()) }
    }

    @Test
    fun `register new cert and access it immediately - opening details after scan`() = runTest2 {
        val instance = createInstance(this)
        instance.registerCertificate(VaccinationTestData.personAVac1QRCode).apply {
            Timber.i("Returned cert is %s", this)
            this.personIdentifier shouldBe
                CertificatePersonIdentifier(
                    dateOfBirthFormatted = "1966-11-11",
                    lastNameStandardized = "ASTRA<EINS",
                    firstNameStandardized = "ANDREAS"
                )
        }

        instance.findCertificateDetails(
            VaccinationCertificateContainerId(VaccinationTestData.personAVac1QRCode.qrCode.toSHA256())
        ) shouldNotBe null
    }

    @Test
    fun `add new certificate - existing data`() = runTest2 {
        val dataBefore = VaccinationTestData.personAData2Vac.copy(
            vaccinations = setOf(VaccinationTestData.personAVac1StoredCertificateData),
        )
        val dataAfter = VaccinationTestData.personAData2Vac.copy(
            vaccinations = setOf(
                VaccinationTestData.personAVac1StoredCertificateData,
                VaccinationTestData.personAVac2StoredCertificateData.copy(
                    scannedAt = nowUTC,
                    certificateSeenByUser = false
                )
            ),
        )
        testStorage = setOf(dataBefore)

        val instance = createInstance(this)

        instance.registerCertificate(VaccinationTestData.personAVac2QRCode).apply {
            Timber.i("Returned cert is %s", this)
            this.personIdentifier shouldBe
                CertificatePersonIdentifier(
                    dateOfBirthFormatted = "1966-11-11",
                    lastNameStandardized = "ASTRA<EINS",
                    firstNameStandardized = "ANDREAS"
                )
        }

        instance.certificates.first().map { it.vaccinationCertificate.qrCodeHash } shouldBe
            dataAfter.vaccinations.map { it.vaccinationQrCode.toSHA256() }
    }

    @Test
    fun `add new certificate - does not match existing person`() = runTest2 {
        testStorage =
            setOf(
                VaccinationTestData.personAData2Vac.copy(
                    vaccinations = VaccinationTestData.personAData2Vac.vaccinations.map {
                        it.copy(certificateSeenByUser = false)
                    }.toSet()
                )
            )

        val instance = createInstance(this)

        every { timeStamper.nowUTC } returns VaccinationTestData.personBData1Vac.vaccinations.single().scannedAt

        instance.registerCertificate(VaccinationTestData.personBVac1QRCode)

        instance.certificates.first().map { it.vaccinationCertificate.qrCodeHash } shouldBe setOf(
            VaccinationTestData.personAData2Vac,
            VaccinationTestData.personBData1Vac
        ).flatMap {
            it.vaccinations.map { c -> c.vaccinationQrCode.toSHA256() }
        }
    }

    @Test
    fun `add new certificate - duplicate certificate`() = runTest2 {
        val dataBefore = VaccinationTestData.personAData2Vac.copy(
            vaccinations = setOf(VaccinationTestData.personAVac1StoredCertificateData),
        )

        testStorage = setOf(dataBefore)
        val instance = createInstance(this)
        shouldThrow<InvalidVaccinationCertificateException> {
            instance.registerCertificate(VaccinationTestData.personAVac1QRCode)
        }.errorCode shouldBe ALREADY_REGISTERED

        instance.certificates.first().map { it.vaccinationCertificate.qrCodeHash } shouldBe
            dataBefore.vaccinations.map { it.vaccinationQrCode.toSHA256() }
    }

    @Test
    fun `clear data`() = runTest2 {
        testStorage = setOf(VaccinationTestData.personAData2Vac)

        val instance = createInstance(this)
        instance.certificates.first().map { it.vaccinationCertificate.qrCodeHash } shouldBe
            VaccinationTestData.personAData2Vac.vaccinations.map { it.vaccinationQrCode.toSHA256() }
        instance.reset()
        testStorage shouldBe emptySet()
        instance.certificates.first() shouldBe emptySet()
    }

    @Test
    fun `remove certificate`() = runTest2 {
        val before = VaccinationTestData.personAData2Vac
        val after = VaccinationTestData.personAData2Vac.copy(
            vaccinations = setOf(VaccinationTestData.personAVac1StoredCertificateData)
        )
        val toRemove = VaccinationTestData.personAVac2StoredCertificateData

        testStorage = setOf(before)

        val instance = createInstance(this)

        instance.certificates.first().map { it.vaccinationCertificate.qrCodeHash } shouldBe
            before.vaccinations.map { it.vaccinationQrCode.toSHA256() }

        instance.deleteCertificate(
            VaccinationCertificateContainerId(toRemove.vaccinationQrCode.toSHA256())
        )

        instance.certificates.first().map { it.vaccinationCertificate.qrCodeHash } shouldBe
            after.vaccinations.map { it.vaccinationQrCode.toSHA256() }
    }

    @Test
    fun `remove certificate - unknown certificate`() = runTest2 {
        testStorage = setOf(VaccinationTestData.personAData2Vac)

        val instance = createInstance(this)
        instance.certificates.first().map { it.vaccinationCertificate.qrCodeHash } shouldBe
            VaccinationTestData.personAData2Vac.vaccinations.map { it.vaccinationQrCode.toSHA256() }
        instance.deleteCertificate(
            VaccinationCertificateContainerId(VaccinationTestData.personBVac1Container.vaccinationQrCode)
        )

        instance.certificates.first().map { it.vaccinationCertificate.qrCodeHash } shouldBe
            VaccinationTestData.personAData2Vac.vaccinations.map { it.vaccinationQrCode.toSHA256() }
    }

    @Test
    fun `remove certificate - last certificate for person`() = runTest2 {
        testStorage = setOf(VaccinationTestData.personBData1Vac)
        val instance = createInstance(this)

        instance.certificates.first().single().vaccinationCertificate.qrCodeHash shouldBe
            VaccinationTestData.personBData1Vac.vaccinations.single().vaccinationQrCode.toSHA256()

        instance.deleteCertificate(
            VaccinationCertificateContainerId(VaccinationTestData.personBVac1Container.vaccinationQrCode.toSHA256())
        )
        instance.certificates.first() shouldBe emptySet()
        testStorage shouldBe emptySet()
    }

    @Test
    fun `storage is not written on init`() = runTest2 {
        val instance = createInstance(this)
        instance.certificates.first()

        coVerify {
            storage.load()
            storage.loadLegacyData()
        }
    }

    @Test
    fun `filter by recycled`() = runTest2 {
        val recycled = VaccinationTestData.personAVac2StoredCertificateData.copy(
            recycledAt = nowUTC
        )
        val notRecycled = VaccinationTestData.personAVac1StoredCertificateData.copy(
            recycledAt = null
        )

        coEvery { storage.load() } returns setOf(recycled, notRecycled)

        createInstance(this).run {
            certificates.first().also {
                it.size shouldBe 1

                val wrapper = it.first()
                wrapper.containerId.qrCodeHash shouldBe notRecycled.vaccinationQrCode.toSHA256()
                wrapper.recycleInfo.isNotRecycled shouldBe true
                wrapper.vaccinationCertificate.state shouldBe CwaCovidCertificate.State.Invalid()
            }

            recycledCertificates.first().also {
                it.size shouldBe 1

                val cert = it.first()
                cert.containerId.qrCodeHash shouldBe recycled.vaccinationQrCode.toSHA256()
                cert.isRecycled shouldBe true
                cert.state shouldBe CwaCovidCertificate.State.Recycled
            }

            allCertificates.first().also {
                it.certificates shouldBe certificates.first()
                it.recycledCertificates.map { c -> c.qrCodeHash } shouldBe
                    recycledCertificates.first().map { c -> c.qrCodeHash }
            }
        }
    }
}
