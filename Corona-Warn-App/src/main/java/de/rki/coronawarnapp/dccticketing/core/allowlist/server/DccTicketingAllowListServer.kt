package de.rki.coronawarnapp.dccticketing.core.allowlist.server

import androidx.annotation.VisibleForTesting
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.allowlist.internal.DccTicketingAllowListException
import de.rki.coronawarnapp.dccticketing.core.allowlist.internal.DccTicketingAllowListException.ErrorCode
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.SignatureValidation
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccTicketingAllowListServer @Inject constructor(
    private val signatureValidation: SignatureValidation,
    private val allowListApi1Lazy: Lazy<DccTicketingAllowListApi1>
) {

    private val allowListApi1 get() = allowListApi1Lazy.get()

    @Throws(DccTicketingAllowListException::class)
    suspend fun getAllowlist(): ByteArray = try {
        Timber.tag(TAG).d("getAllowList()")
        allowListApi1.allowList().parseAndValidate()
    } catch (e: Exception) {
        if (e is DccTicketingAllowListException) throw e
        Timber.tag(TAG).e(e, "Getting DccTicketing Allowlist from server failed cause: ${e.message}")
        throw when (e) {
            is CwaUnknownHostException,
            is NetworkReadTimeoutException,
            is NetworkConnectTimeoutException -> ErrorCode.NO_NETWORK
            is CwaClientError -> ErrorCode.CLIENT_ERROR
            else -> ErrorCode.SERVER_ERROR
        }.let { DccTicketingAllowListException(errorCode = it, cause = e) }
    }

    @VisibleForTesting
    internal fun Response<ResponseBody>.parseAndValidate(): ByteArray {
        if (!isSuccessful) throw HttpException(this)

        val fileMap = requireNotNull(body()) { "Response was successful but body was null" }
            .byteStream().unzip().readIntoMap()

        val exportBinary = fileMap[EXPORT_BINARY_FILE_NAME]
        val exportSignature = fileMap[EXPORT_SIGNATURE_FILE_NAME]

        if (exportBinary == null || exportSignature == null) throw DccTicketingAllowListException(
            ErrorCode.FILE_MISSING
        )

        val isSignatureValid = signatureValidation.hasValidSignature(
            toVerify = exportBinary,
            signatureList = SignatureValidation.parseTEKStyleSignature(exportSignature)
        )
        if (!isSignatureValid) throw DccTicketingAllowListException(ErrorCode.SIGNATURE_INVALID)

        return exportBinary
    }

    companion object {
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"

        private val TAG = tag<DccTicketingAllowListServer>()
    }
}
