package de.rki.coronawarnapp.dccticketing.core.allowlist.api

import androidx.annotation.VisibleForTesting
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.signature.core.common.exception.DscValidationException
import de.rki.coronawarnapp.server.protocols.internal.dgc.ValidationServiceAllowlistOuterClass.ValidationServiceAllowlist
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
    private val allowListApi1: Lazy<DccTicketingAllowListApi1>
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getAllowlist(): ValidationServiceAllowlist = try {
        Timber.d("getAllowList()")
        val byteArray = allowListApi1.get().allowList().validate()
        ValidationServiceAllowlist.parseFrom(byteArray)
    } catch (e: Exception) {
        if (e is DscValidationException) throw e
        Timber.e(e, "Getting DccTicketing Allowlist from server failed cause: ${e.message}")
        throw DscValidationException(DscValidationException.ErrorCode.SERVER_ERROR)
    }

    @VisibleForTesting
    internal fun Response<ResponseBody>.validate(): ByteArray {
        if (!isSuccessful) throw HttpException(this)

        val fileMap = requireNotNull(body()) { "Response was successful but body was null" }
            .byteStream().unzip().readIntoMap()

        val exportBinary = fileMap[EXPORT_BINARY_FILE_NAME]
        val exportSignature = fileMap[EXPORT_SIGNATURE_FILE_NAME]

        if (exportBinary == null || exportSignature == null) throw DscValidationException(
            DscValidationException.ErrorCode.FILE_MISSING
        )

        val isSignatureValid = signatureValidation.hasValidSignature(
            toVerify = exportBinary,
            signatureList = SignatureValidation.parseTEKStyleSignature(exportSignature)
        )
        if (!isSignatureValid) throw DscValidationException(DscValidationException.ErrorCode.SIGNATURE_INVALID)

        return exportBinary
    }

    companion object {
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
    }
}
