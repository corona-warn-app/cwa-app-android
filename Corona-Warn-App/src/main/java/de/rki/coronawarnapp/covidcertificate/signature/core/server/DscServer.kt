package de.rki.coronawarnapp.covidcertificate.signature.core.server

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.covidcertificate.signature.core.common.exception.DscValidationException
import de.rki.coronawarnapp.covidcertificate.signature.core.common.exception.DscValidationException.ErrorCode
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.SignatureValidation
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DscServer @Inject constructor(
    private val signatureValidation: SignatureValidation,
    private val dscApi: DscApiV1
) {

    suspend fun getDscList(): ByteArray? {
        return try {
            Timber.d("getDscList()")
            dscApi.dscList().parseAndValidate()
        } catch (e: Exception) {
            Timber.e(e, "Getting List of DSCs from server failed cause: ${e.message}")
            null
        }
    }

    @VisibleForTesting
    internal fun Response<ResponseBody>.parseAndValidate(): ByteArray {
        if (!isSuccessful) throw HttpException(this)

        val fileMap = requireNotNull(body()) { "Response was successful but body was null" }
            .byteStream().unzip().readIntoMap()

        val exportBinary = fileMap[EXPORT_BINARY_FILE_NAME]
        val exportSignature = fileMap[EXPORT_SIGNATURE_FILE_NAME]

        if (exportBinary == null || exportSignature == null) throw DscValidationException(ErrorCode.FILE_MISSING)

        val isSignatureValid = signatureValidation.hasValidSignature(
            toVerify = exportBinary,
            signatureList = SignatureValidation.parseTEKStyleSignature(exportSignature)
        )
        if (!isSignatureValid) throw DscValidationException(ErrorCode.SIGNATURE_INVALID)

        return exportBinary
    }

    companion object {
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
    }
}
