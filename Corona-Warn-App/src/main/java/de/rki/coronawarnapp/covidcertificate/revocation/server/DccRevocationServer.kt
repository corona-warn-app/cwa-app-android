package de.rki.coronawarnapp.covidcertificate.revocation.server

import androidx.annotation.VisibleForTesting
import dagger.Lazy
import de.rki.coronawarnapp.covidcertificate.revocation.error.DccRevocationErrorCode
import de.rki.coronawarnapp.covidcertificate.revocation.error.DccRevocationException
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationKidTypeIndex
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationHashType
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidList
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.security.SignatureValidation
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okio.ByteString
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccRevocationServer @Inject constructor(
    private val revocationApiLazy: Lazy<DccRevocationApi>,
    private val dispatcherProvider: DispatcherProvider,
    private val signatureValidation: SignatureValidation,
    private val revocationParser: DccRevocationParser
) {

    private val dccRevocationApi: DccRevocationApi
        get() = revocationApiLazy.get()

    @Throws(DccRevocationException::class)
    suspend fun getRevocationKidList(): RevocationKidList = execute(
        noNetworkErrorCodeDcc = DccRevocationErrorCode.DCC_RL_KID_LIST_NO_NETWORK,
        clientErrorCodeDcc = DccRevocationErrorCode.DCC_RL_KID_LIST_CLIENT_ERRORDcc,
        serverErrorCodeDcc = DccRevocationErrorCode.DCC_RL_KID_LIST_SERVER_ERRORDcc
    ) {
        Timber.tag(TAG).d("getRevocationKidList()")
        val response = dccRevocationApi.getRevocationKidList()
        val rawData = response.parseAndValidate(
            parseErrorCodeDcc = DccRevocationErrorCode.DCC_RL_KID_LIST_INVALID_SIGNATURE
        )

        return@execute revocationParser.kidListFrom(rawData).also {
            Timber.tag(TAG).d("returning kid list with %d items", it.items.size)
        }
    }

    @Throws(DccRevocationException::class)
    suspend fun getRevocationKidTypeIndex(
        kid: ByteString,
        hashType: RevocationHashType
    ): CachedRevocationKidTypeIndex = execute(
        noNetworkErrorCodeDcc = DccRevocationErrorCode.DCC_RL_KT_IDX_NO_NETWORK,
        clientErrorCodeDcc = DccRevocationErrorCode.DCC_RL_KT_IDX_CLIENT_ERRORDcc,
        serverErrorCodeDcc = DccRevocationErrorCode.DCC_RL_KT_IDX_SERVER_ERRORDcc
    ) {
        Timber.tag(TAG).d("getRevocationKidTypeIndex(kid=%s, hashType=%s)", kid, hashType)
        val response = dccRevocationApi.getRevocationKidTypeIndex(kid = kid.hex(), type = hashType.type)
        val rawData = response.parseAndValidate(
            parseErrorCodeDcc = DccRevocationErrorCode.DCC_RL_KT_IDX_INVALID_SIGNATURE
        )
        val revocationKidTypeIndex = revocationParser.kidTypeIndexFrom(rawData)

        return@execute CachedRevocationKidTypeIndex(
            kid = kid,
            hashType = hashType,
            revocationKidTypeIndex = revocationKidTypeIndex
        ).also { Timber.tag(TAG).d("returning %s", it) }
    }

    @Throws(DccRevocationException::class)
    suspend fun getRevocationChunk(
        kid: ByteString,
        hashType: RevocationHashType,
        x: ByteString,
        y: ByteString
    ): CachedRevocationChunk = execute(
        noNetworkErrorCodeDcc = DccRevocationErrorCode.DCC_RL_KTXY_CHUNK_NO_NETWORK,
        clientErrorCodeDcc = DccRevocationErrorCode.DCC_RL_KTXY_CHUNK_CLIENT_ERRORDcc,
        serverErrorCodeDcc = DccRevocationErrorCode.DCC_RL_KTXY_CHUNK_SERVER_ERRORDcc
    ) {
        Timber.tag(TAG).d(
            "getRevocationChunk(kid=%s, hashType=%s, x=%s, y=%s)",
            kid,
            hashType,
            x,
            y
        )
        val response = dccRevocationApi.getRevocationChunk(
            kid = kid.hex(),
            type = hashType.type,
            x = x.hex(),
            y = y.hex()
        )
        val rawData = response.parseAndValidate(
            parseErrorCodeDcc = DccRevocationErrorCode.DCC_RL_KTXY_INVALID_SIGNATURE
        )
        val revocationChunk = revocationParser.chunkFrom(rawData)
        val revocationEntryCoordinates = RevocationEntryCoordinates(kid = kid, type = hashType, x = x, y = y)

        return@execute CachedRevocationChunk(
            coordinates = revocationEntryCoordinates,
            revocationChunk = revocationChunk
        ).also { Timber.tag(TAG).d("returning %s", it) }
    }

    private suspend fun <T> execute(
        noNetworkErrorCodeDcc: DccRevocationErrorCode,
        clientErrorCodeDcc: DccRevocationErrorCode,
        serverErrorCodeDcc: DccRevocationErrorCode,
        block: suspend () -> T
    ): T = withContext(dispatcherProvider.IO) {
        try {
            block()
        } catch (e: Exception) {
            if (e is DccRevocationException) throw e
            Timber.tag(TAG).w(e, "Request failed")
            throw when (e) {
                is CwaUnknownHostException,
                is NetworkReadTimeoutException,
                is NetworkConnectTimeoutException -> noNetworkErrorCodeDcc
                is CwaClientError -> clientErrorCodeDcc
                else -> serverErrorCodeDcc
            }.let { DccRevocationException(errorCodeDcc = it, cause = e) }
        }
    }

    private fun Response<ResponseBody>.parseAndValidate(parseErrorCodeDcc: DccRevocationErrorCode): ByteArray = try {
        if (!isSuccessful) throw HttpException(this)

        val fileMap = requireNotNull(body()) { "Response was successful but body was null" }
            .byteStream().unzip().readIntoMap()

        val exportBinary = fileMap[EXPORT_BINARY_FILE_NAME]
        val exportSignature = fileMap[EXPORT_SIGNATURE_FILE_NAME]

        checkNotNull(exportBinary) { "exportBinary is null" }
        checkNotNull(exportSignature) { "exportSignature is null" }

        val isSignatureValid = signatureValidation.hasValidSignature(
            toVerify = exportBinary,
            signatureList = SignatureValidation.parseTEKStyleSignature(exportSignature)
        )
        if (!isSignatureValid) throw DccRevocationException(errorCodeDcc = parseErrorCodeDcc)

        exportBinary
    } catch (e: Exception) {
        throw when (e) {
            is DccRevocationException -> e
            else -> DccRevocationException(errorCodeDcc = parseErrorCodeDcc, cause = e)
        }
    }
}

private val TAG = tag<DccRevocationServer>()

@VisibleForTesting const val EXPORT_BINARY_FILE_NAME = "export.bin"
@VisibleForTesting const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
