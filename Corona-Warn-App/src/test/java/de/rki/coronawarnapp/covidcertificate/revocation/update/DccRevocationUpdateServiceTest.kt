package de.rki.coronawarnapp.covidcertificate.revocation.update

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.DccRevocationCalculationTestCase
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.DccRevocationCalculationTestCaseProvider
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.kidHash
import de.rki.coronawarnapp.covidcertificate.revocation.check.DccRevocationChecker
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationKidTypeIndex
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationHashType
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidList
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidListItem
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidTypeIndex
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidTypeIndexItem
import de.rki.coronawarnapp.covidcertificate.revocation.server.DccRevocationServer
import de.rki.coronawarnapp.covidcertificate.revocation.storage.DccRevocationRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTest
import javax.inject.Inject

class DccRevocationUpdateServiceTest : BaseTest() {

    @MockK lateinit var dccRevocationServer: DccRevocationServer
    @MockK lateinit var dccRevocationChecker: DccRevocationChecker
    @RelaxedMockK lateinit var dccRevocationRepository: DccRevocationRepository

    @Inject lateinit var dccQrCodeExtractor: DccQrCodeExtractor

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        DaggerCovidCertificateTestComponent.factory().create().inject(this)
    }

    private val instance: DccRevocationUpdateService
        get() = DccRevocationUpdateService(
            revocationServer = dccRevocationServer,
            revocationRepository = dccRevocationRepository,
            dccRevocationChecker = dccRevocationChecker
        )

    @ParameterizedTest
    @ArgumentsSource(DccRevocationCalculationTestCaseProvider::class)
    fun `Happy path`(testCase: DccRevocationCalculationTestCase) = runBlockingTest {
        testCase.checkFor(RevocationHashType.SIGNATURE)
        testCase.checkFor(RevocationHashType.UCI)
        testCase.checkFor(RevocationHashType.COUNTRYCODEUCI)
    }

    private suspend fun DccRevocationCalculationTestCase.checkFor(hashType: RevocationHashType) {
        val cert = toCert()
        val kidHash = cert.dccData.kidHash()
        val chunk = toChunk(kidHash, hashType)
        val index = CachedRevocationKidTypeIndex(
            kid = kidHash,
            hashType = hashType,
            revocationKidTypeIndex = RevocationKidTypeIndex(
                listOf(
                    RevocationKidTypeIndexItem(x = chunk.coordinates.x, y = listOf(chunk.coordinates.y))
                )
            )
        )
        val kidList = RevocationKidList(
            items = setOf(
                RevocationKidListItem(kid = kidHash, hashTypes = setOf(hashType))
            )
        )

        every { dccRevocationChecker.isRevoked(dccData = any(), any()) } returns false
        coEvery { dccRevocationServer.getRevocationKidList() } returns kidList
        coEvery { dccRevocationServer.getRevocationKidTypeIndex(any(), any()) } returns index
        coEvery { dccRevocationServer.getRevocationChunk(any(), any(), any(), any()) } returns chunk

        instance.updateRevocationList(allCertificates = setOf(cert))

        // Just mockk things ¯\_(ツ)_/¯
        val data = cert.dccData
        coVerify {
            with(dccRevocationServer) {
                getRevocationKidList()
                getRevocationKidTypeIndex(kid = kidHash, hashType = hashType)
                getRevocationChunk(kid = kidHash, hashType = hashType, x = chunk.coordinates.x, y = chunk.coordinates.y)
            }
            dccRevocationChecker.isRevoked(dccData = data, listOf())
            dccRevocationRepository.saveCachedRevocationChunks(setOf(chunk))
        }
    }

    private fun DccRevocationCalculationTestCase.toChunk(
        kid: ByteString,
        type: RevocationHashType
    ): CachedRevocationChunk {
        val hash = when (type) {
            RevocationHashType.SIGNATURE -> expSIGNATURE
            RevocationHashType.UCI -> expUCI
            RevocationHashType.COUNTRYCODEUCI -> expCOUNTRYCODEUCI
        }.decodeHex()

        val coord = RevocationEntryCoordinates(
            kid = kid,
            type = type,
            x = hash.substring(0, 1),
            y = hash.substring(1, 2)
        )

        val revocationChunk = RevocationChunk(hashes = listOf(hash))
        return CachedRevocationChunk(
            coordinates = coord,
            revocationChunk = revocationChunk
        )
    }

    private suspend fun DccRevocationCalculationTestCase.toCert() = when {
        description.contains("REC") -> createCert<RecoveryCertificate>(barcodeData)
        else -> createCert<VaccinationCertificate>(barcodeData)
    }

    private suspend inline fun <reified T : CwaCovidCertificate> createCert(barcodeData: String): T = mockk {
        every { dccData } returns createCertData(barcodeData)
    }

    private suspend fun createCertData(barcodeData: String) = dccQrCodeExtractor.extract(barcodeData).data
}
