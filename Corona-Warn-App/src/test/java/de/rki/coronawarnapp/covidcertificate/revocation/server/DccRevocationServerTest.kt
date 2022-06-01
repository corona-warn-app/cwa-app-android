package de.rki.coronawarnapp.covidcertificate.revocation.server

import de.rki.coronawarnapp.covidcertificate.revocation.error.DccRevocationErrorCode
import de.rki.coronawarnapp.covidcertificate.revocation.error.DccRevocationException
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationKidTypeIndex
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationHashType
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidList
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidListItem
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidTypeIndex
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidTypeIndexItem
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.util.HashExtensions.sha256
import de.rki.coronawarnapp.util.security.SignatureValidation
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runTest2
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DccRevocationServerTest : BaseTest() {

    @MockK lateinit var revocationApi: DccRevocationApi
    @MockK lateinit var signatureValidation: SignatureValidation
    @MockK lateinit var revocationParser: DccRevocationParser

    private val exportSignature = "exportSignature".toByteArray()
    private val exportBinaryKidList = "exportBinaryKidList".toByteArray()
    private val exportBinaryKidTypeIndex = "exportBinaryKidTypeIndex".toByteArray()
    private val exportBinaryChunk = "exportBinaryChunk".toByteArray()

    private val kid = "kid".sha256()
    private val hashType = RevocationHashType.SIGNATURE
    private val x = "x".sha256()
    private val y = "y".sha256()

    private val instance: DccRevocationServer
        get() = DccRevocationServer(
            revocationApiLazy = { revocationApi },
            dispatcherProvider = TestDispatcherProvider(),
            signatureValidation = signatureValidation,
            revocationParser = revocationParser
        )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { signatureValidation.hasValidSignature(any(), any()) } returns true

        coEvery { revocationApi.getRevocationKidList() } returns Response.success(
            createBundledResponse(exportBinaryKidList)
        )

        coEvery { revocationApi.getRevocationKidTypeIndex(any(), any()) } returns Response.success(
            createBundledResponse(exportBinaryKidTypeIndex)
        )

        coEvery { revocationApi.getRevocationChunk(any(), any(), any(), any()) } returns Response.success(
            createBundledResponse(exportBinaryChunk)
        )

        mockkObject(SignatureValidation)
        every { SignatureValidation.parseTEKStyleSignature(exportSignature) } returns emptySequence()
    }

    @Test
    fun `happy path - getRevocationKidList`() = runTest2 {
        val kidList = RevocationKidList(
            items = setOf(
                RevocationKidListItem(
                    kid = "kid".sha256(),
                    hashTypes = setOf(RevocationHashType.UCI)
                )
            )
        )

        coEvery { revocationParser.kidListFrom(exportBinaryKidList) } returns kidList

        instance.getRevocationKidList() shouldBe kidList

        coVerify {
            revocationApi.getRevocationKidList()
            signatureValidation.hasValidSignature(exportBinaryKidList, any())
            revocationParser.kidListFrom(exportBinaryKidList)
        }
    }

    @Test
    fun `happy path - getRevocationKidTypeIndex`() = runTest2 {
        val kidTypeIndex = CachedRevocationKidTypeIndex(
            kid = kid,
            hashType = hashType,
            revocationKidTypeIndex = RevocationKidTypeIndex(
                items = listOf(
                    RevocationKidTypeIndexItem(
                        x = "x".sha256(),
                        y = listOf("y".sha256())
                    )
                )
            )
        )

        coEvery { revocationParser.kidTypeIndexFrom(exportBinaryKidTypeIndex) } returns
            kidTypeIndex.revocationKidTypeIndex

        instance.getRevocationKidTypeIndex(kid, hashType) shouldBe kidTypeIndex

        coVerify {
            revocationApi.getRevocationKidTypeIndex(kid.hex(), hashType.type)
            signatureValidation.hasValidSignature(exportBinaryKidTypeIndex, any())
            revocationParser.kidTypeIndexFrom(exportBinaryKidTypeIndex)
        }
    }

    @Test
    fun `happy path - getRevocationChunk`() = runTest2 {
        val chunk = CachedRevocationChunk(
            coordinates = RevocationEntryCoordinates(
                kid = kid,
                type = hashType,
                x = x,
                y = y
            ),
            revocationChunk = RevocationChunk(hashes = listOf("RevocationChunk".sha256()))
        )

        coEvery { revocationParser.chunkFrom(exportBinaryChunk) } returns chunk.revocationChunk

        instance.getRevocationChunk(kid, hashType, x, y) shouldBe chunk

        coVerify {
            revocationApi.getRevocationChunk(kid.hex(), hashType.type, x.hex(), y.hex())
            signatureValidation.hasValidSignature(exportBinaryChunk, any())
            revocationParser.chunkFrom(exportBinaryChunk)
        }
    }

    @Test
    fun `reports invalid signature`() = runTest2 {
        every { signatureValidation.hasValidSignature(any(), any()) } returns false

        with(instance) {
            shouldThrow<DccRevocationException> {
                getRevocationKidList()
            }.errorCode shouldBe DccRevocationErrorCode.DCC_RL_KID_LIST_INVALID_SIGNATURE

            shouldThrow<DccRevocationException> {
                getRevocationKidTypeIndex(kid, hashType)
            }.errorCode shouldBe DccRevocationErrorCode.DCC_RL_KT_IDX_INVALID_SIGNATURE

            shouldThrow<DccRevocationException> {
                getRevocationChunk(kid, hashType, x, y)
            }.errorCode shouldBe DccRevocationErrorCode.DCC_RL_KTXY_INVALID_SIGNATURE
        }

        every { signatureValidation.hasValidSignature(any(), any()) } returns true

        fun invalid() = "Invalid Response".toResponseBody()
        coEvery { revocationApi.getRevocationKidList() } returns Response.success(invalid())

        coEvery { revocationApi.getRevocationKidTypeIndex(any(), any()) } returns Response.success(invalid())

        coEvery { revocationApi.getRevocationChunk(any(), any(), any(), any()) } returns Response.success(invalid())

        with(instance) {
            shouldThrow<DccRevocationException> {
                getRevocationKidList()
            }.also {
                it.errorCode shouldBe DccRevocationErrorCode.DCC_RL_KID_LIST_INVALID_SIGNATURE
                it.cause should beInstanceOf(IllegalStateException::class)
            }

            shouldThrow<DccRevocationException> {
                getRevocationKidTypeIndex(kid, hashType)
            }.also {
                it.errorCode shouldBe DccRevocationErrorCode.DCC_RL_KT_IDX_INVALID_SIGNATURE
                it.cause should beInstanceOf(IllegalStateException::class)
            }

            shouldThrow<DccRevocationException> {
                getRevocationChunk(kid, hashType, x, y)
            }.also {
                it.errorCode shouldBe DccRevocationErrorCode.DCC_RL_KTXY_INVALID_SIGNATURE
                it.cause should beInstanceOf(IllegalStateException::class)
            }
        }
    }

    @Test
    fun `reports no network`() = runTest2 {
        val error = NetworkConnectTimeoutException(message = "Test error")
        coEvery { revocationApi.getRevocationKidList() } throws error
        coEvery { revocationApi.getRevocationKidTypeIndex(any(), any()) } throws error
        coEvery { revocationApi.getRevocationChunk(any(), any(), any(), any()) } throws error

        with(instance) {
            shouldThrow<DccRevocationException> {
                getRevocationKidList()
            }.errorCode shouldBe DccRevocationErrorCode.DCC_RL_KID_LIST_NO_NETWORK

            shouldThrow<DccRevocationException> {
                getRevocationKidTypeIndex(kid, hashType)
            }.errorCode shouldBe DccRevocationErrorCode.DCC_RL_KT_IDX_NO_NETWORK

            shouldThrow<DccRevocationException> {
                getRevocationChunk(kid, hashType, x, y)
            }.errorCode shouldBe DccRevocationErrorCode.DCC_RL_KTXY_CHUNK_NO_NETWORK
        }
    }

    @Test
    fun `reports client error`() = runTest2 {
        val error = CwaClientError(statusCode = 400, message = "Test error")
        coEvery { revocationApi.getRevocationKidList() } throws error
        coEvery { revocationApi.getRevocationKidTypeIndex(any(), any()) } throws error
        coEvery { revocationApi.getRevocationChunk(any(), any(), any(), any()) } throws error

        with(instance) {
            shouldThrow<DccRevocationException> {
                getRevocationKidList()
            }.errorCode shouldBe DccRevocationErrorCode.DCC_RL_KID_LIST_CLIENT_ERROR

            shouldThrow<DccRevocationException> {
                getRevocationKidTypeIndex(kid, hashType)
            }.errorCode shouldBe DccRevocationErrorCode.DCC_RL_KT_IDX_CLIENT_ERROR

            shouldThrow<DccRevocationException> {
                getRevocationChunk(kid, hashType, x, y)
            }.errorCode shouldBe DccRevocationErrorCode.DCC_RL_KTXY_CHUNK_CLIENT_ERROR
        }
    }

    @Test
    fun `reports server error`() = runTest2 {
        val error = CwaServerError(statusCode = 500, message = "Test error")
        coEvery { revocationApi.getRevocationKidList() } throws error
        coEvery { revocationApi.getRevocationKidTypeIndex(any(), any()) } throws error
        coEvery { revocationApi.getRevocationChunk(any(), any(), any(), any()) } throws error

        with(instance) {
            shouldThrow<DccRevocationException> {
                getRevocationKidList()
            }.errorCode shouldBe DccRevocationErrorCode.DCC_RL_KID_LIST_SERVER_ERROR

            shouldThrow<DccRevocationException> {
                getRevocationKidTypeIndex(kid, hashType)
            }.errorCode shouldBe DccRevocationErrorCode.DCC_RL_KT_IDX_SERVER_ERROR

            shouldThrow<DccRevocationException> {
                getRevocationChunk(kid, hashType, x, y)
            }.errorCode shouldBe DccRevocationErrorCode.DCC_RL_KTXY_CHUNK_SERVER_ERROR
        }
    }

    private fun createBundledResponse(exportBinary: ByteArray): ResponseBody = ByteArrayOutputStream().use { bos ->
        ZipOutputStream(bos).use { zipOutputStream ->
            val zipEntrySignature = ZipEntry(EXPORT_SIGNATURE_FILE_NAME)
            zipOutputStream.putNextEntry(zipEntrySignature)
            zipOutputStream.write(exportSignature)
            zipOutputStream.closeEntry()

            val zipEntryBinary = ZipEntry(EXPORT_BINARY_FILE_NAME)
            zipOutputStream.putNextEntry(zipEntryBinary)
            zipOutputStream.write(exportBinary)
            zipOutputStream.closeEntry()
        }

        bos.toByteArray().toResponseBody()
    }
}
