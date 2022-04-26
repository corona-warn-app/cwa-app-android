package de.rki.coronawarnapp.covidcertificate.revocation.check

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.DccRevocationCalculationTestCase
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.DccRevocationCalculationTestCaseProvider
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationHashType
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

import testhelpers.BaseTest
import javax.inject.Inject

class DccRevocationCheckerTest : BaseTest() {

    @Inject lateinit var dccQrCodeExtractor: DccQrCodeExtractor

    @BeforeEach
    fun setup() {
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
    }

    @ParameterizedTest
    @ArgumentsSource(DccRevocationCalculationTestCaseProvider::class)
    fun `all not revoked when no revocation list`(testCaseDcc: DccRevocationCalculationTestCase) = runBlockingTest {
        val dccData = dccQrCodeExtractor.extract(testCaseDcc.barcodeData).data
        DccRevocationChecker().isRevoked(dccData, listOf()) shouldBe false
    }

    @ParameterizedTest
    @ArgumentsSource(DccRevocationCalculationTestCaseProvider::class)
    fun `all revoked for type SIGNATURE`(testCaseDcc: DccRevocationCalculationTestCase) = runBlockingTest {
        val dccData = dccQrCodeExtractor.extract(testCaseDcc.barcodeData).data
        DccRevocationChecker().isRevoked(
            dccData,
            listOf(
                CachedRevocationChunk(
                    coordinates = RevocationEntryCoordinates(
                        kid = "138291851ea0ddb4".decodeHex(),
                        type = RevocationHashType.SIGNATURE,
                        x = "26".decodeHex(),
                        y = "98".decodeHex()
                    ),
                    revocationChunk = RevocationChunk(
                        hashes = listOf(
                            "2698afff2cdbd017bc957b6fa1764ac7".decodeHex()
                        )
                    )
                ),
                CachedRevocationChunk(
                    coordinates = RevocationEntryCoordinates(
                        kid = "75d52ef3c0a6be96".decodeHex(),
                        type = RevocationHashType.SIGNATURE,
                        x = "b8".decodeHex(),
                        y = "03".decodeHex()
                    ),
                    revocationChunk = RevocationChunk(
                        hashes = listOf(
                            "b80307723714cb7cd9a653a0bc56c89d".decodeHex()
                        )
                    )
                ),
                CachedRevocationChunk(
                    coordinates = RevocationEntryCoordinates(
                        kid = "9795a12910235933".decodeHex(),
                        type = RevocationHashType.SIGNATURE,
                        x = "fa".decodeHex(),
                        y = "6a".decodeHex()
                    ),
                    revocationChunk = RevocationChunk(
                        hashes = listOf(
                            "fa6af59289198a146c35710b67cdd415".decodeHex()
                        )
                    )
                ),
            )
        ) shouldBe true
    }

    @ParameterizedTest
    @ArgumentsSource(DccRevocationCalculationTestCaseProvider::class)
    fun `all revoked for type UCI`(testCaseDcc: DccRevocationCalculationTestCase) = runBlockingTest {
        val dccData = dccQrCodeExtractor.extract(testCaseDcc.barcodeData).data
        DccRevocationChecker().isRevoked(
            dccData,
            listOf(
                CachedRevocationChunk(
                    coordinates = RevocationEntryCoordinates(
                        kid = "138291851ea0ddb4".decodeHex(),
                        type = RevocationHashType.UCI,
                        x = "42".decodeHex(),
                        y = "3b".decodeHex()
                    ),
                    revocationChunk = RevocationChunk(
                        hashes = listOf(
                            "423bd0a847634ce8dea5dd5e9400f287".decodeHex()
                        )
                    )
                ),
                CachedRevocationChunk(
                    coordinates = RevocationEntryCoordinates(
                        kid = "75d52ef3c0a6be96".decodeHex(),
                        type = RevocationHashType.UCI,
                        x = "54".decodeHex(),
                        y = "a9".decodeHex()
                    ),
                    revocationChunk = RevocationChunk(
                        hashes = listOf(
                            "54a90383b0e792489835caec8befd2a4".decodeHex()
                        )
                    )
                ),
                CachedRevocationChunk(
                    coordinates = RevocationEntryCoordinates(
                        kid = "9795a12910235933".decodeHex(),
                        type = RevocationHashType.UCI,
                        x = "3d".decodeHex(),
                        y = "59".decodeHex()
                    ),
                    revocationChunk = RevocationChunk(
                        hashes = listOf(
                            "3d59bc9b9563dcea32836578c9d3b161".decodeHex()
                        )
                    )
                ),
            )
        ) shouldBe true
    }

    @ParameterizedTest
    @ArgumentsSource(DccRevocationCalculationTestCaseProvider::class)
    fun `all revoked for type COUNTRYCODEUCI`(testCaseDcc: DccRevocationCalculationTestCase) = runBlockingTest {
        val dccData = dccQrCodeExtractor.extract(testCaseDcc.barcodeData).data
        DccRevocationChecker().isRevoked(
            dccData,
            listOf(
                CachedRevocationChunk(
                    coordinates = RevocationEntryCoordinates(
                        kid = "138291851ea0ddb4".decodeHex(),
                        type = RevocationHashType.COUNTRYCODEUCI,
                        x = "7d".decodeHex(),
                        y = "fb".decodeHex()
                    ),
                    revocationChunk = RevocationChunk(
                        hashes = listOf(
                            "7dfb27c9adf0551f0600585a9641a026".decodeHex()
                        )
                    )
                ),
                CachedRevocationChunk(
                    coordinates = RevocationEntryCoordinates(
                        kid = "75d52ef3c0a6be96".decodeHex(),
                        type = RevocationHashType.COUNTRYCODEUCI,
                        x = "ee".decodeHex(),
                        y = "ab".decodeHex()
                    ),
                    revocationChunk = RevocationChunk(
                        hashes = listOf(
                            "eeabbcbbb1637ddf343458f1f41842da".decodeHex()
                        )
                    )
                ),
                CachedRevocationChunk(
                    coordinates = RevocationEntryCoordinates(
                        kid = "9795a12910235933".decodeHex(),
                        type = RevocationHashType.COUNTRYCODEUCI,
                        x = "0c".decodeHex(),
                        y = "32".decodeHex()
                    ),
                    revocationChunk = RevocationChunk(
                        hashes = listOf(
                            "0c32d9ec22eb7167ea0e7a1eb0a21849".decodeHex()
                        )
                    )
                ),
            )
        ) shouldBe true
    }

    @ParameterizedTest
    @ArgumentsSource(DccRevocationCalculationTestCaseProvider::class)
    fun `all revoked for combination`(testCaseDcc: DccRevocationCalculationTestCase) = runBlockingTest {
        val dccData = dccQrCodeExtractor.extract(testCaseDcc.barcodeData).data
        DccRevocationChecker().isRevoked(
            dccData,
            listOf(
                CachedRevocationChunk(
                    coordinates = RevocationEntryCoordinates(
                        kid = "138291851ea0ddb4".decodeHex(),
                        type = RevocationHashType.SIGNATURE,
                        x = "26".decodeHex(),
                        y = "98".decodeHex()
                    ),
                    revocationChunk = RevocationChunk(
                        hashes = listOf(
                            "2698afff2cdbd017bc957b6fa1764ac7".decodeHex()
                        )
                    )
                ),
                CachedRevocationChunk(
                    coordinates = RevocationEntryCoordinates(
                        kid = "75d52ef3c0a6be96".decodeHex(),
                        type = RevocationHashType.UCI,
                        x = "54".decodeHex(),
                        y = "a9".decodeHex()
                    ),
                    revocationChunk = RevocationChunk(
                        hashes = listOf(
                            "54a90383b0e792489835caec8befd2a4".decodeHex()
                        )
                    )
                ),
                CachedRevocationChunk(
                    coordinates = RevocationEntryCoordinates(
                        kid = "9795a12910235933".decodeHex(),
                        type = RevocationHashType.COUNTRYCODEUCI,
                        x = "0c".decodeHex(),
                        y = "32".decodeHex()
                    ),
                    revocationChunk = RevocationChunk(
                        hashes = listOf(
                            "0c32d9ec22eb7167ea0e7a1eb0a21849".decodeHex()
                        )
                    )
                ),
            )
        ) shouldBe true
    }

    @ParameterizedTest
    @ArgumentsSource(DccRevocationCalculationTestCaseProvider::class)
    fun `all not revoked when coordinate exist but no hashes`(testCaseDcc: DccRevocationCalculationTestCase) =
        runBlockingTest {
            val dccData = dccQrCodeExtractor.extract(testCaseDcc.barcodeData).data
            DccRevocationChecker().isRevoked(
                dccData,
                listOf(
                    CachedRevocationChunk(
                        coordinates = RevocationEntryCoordinates(
                            kid = "138291851ea0ddb4".decodeHex(),
                            type = RevocationHashType.SIGNATURE,
                            x = "26".decodeHex(),
                            y = "98".decodeHex()
                        ),
                        revocationChunk = RevocationChunk(
                            hashes = listOf()
                        )
                    ),
                    CachedRevocationChunk(
                        coordinates = RevocationEntryCoordinates(
                            kid = "75d52ef3c0a6be96".decodeHex(),
                            type = RevocationHashType.UCI,
                            x = "54".decodeHex(),
                            y = "a9".decodeHex()
                        ),
                        revocationChunk = RevocationChunk(
                            hashes = listOf()
                        )
                    ),
                    CachedRevocationChunk(
                        coordinates = RevocationEntryCoordinates(
                            kid = "9795a12910235933".decodeHex(),
                            type = RevocationHashType.COUNTRYCODEUCI,
                            x = "0c".decodeHex(),
                            y = "32".decodeHex()
                        ),
                        revocationChunk = RevocationChunk(
                            hashes = listOf()
                        )
                    ),
                )
            ) shouldBe false
        }

    @Test
    fun `isRevoked - kid is empty`() {
        val dccData = mockk<DccData<*>>().apply { every { kid } returns "" }
        DccRevocationChecker().isRevoked(dccData = dccData, revocationList = listOf()) shouldBe false
    }
}
