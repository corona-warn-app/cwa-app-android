package de.rki.coronawarnapp.presencetracing.locations.server.qrcodepostertemplate

import androidx.annotation.VisibleForTesting
import com.google.protobuf.InvalidProtocolBufferException
import de.rki.coronawarnapp.server.protocols.internal.pt.QrCodePosterTemplate
import de.rki.coronawarnapp.util.ZipHelper.readIntoMap
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.SignatureValidation
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QrCodePosterTemplateServer @Inject constructor(
    private val api: QrCodePosterTemplateApiV1,
    private val defaultTemplateSource: DefaultQrCodePosterTemplateSource,
    private val signatureValidation: SignatureValidation
) {
    suspend fun downloadQrCodePosterTemplate(): QrCodePosterTemplate.QRCodePosterTemplateAndroid {

        Timber.d("Start download of QR-Code poster template.")
        val binaryTemplate = getTemplateFromApiOrCache()

        return try {
            QrCodePosterTemplate.QRCodePosterTemplateAndroid.parseFrom(binaryTemplate)
        } catch (exception: InvalidProtocolBufferException) {
            throw QrCodePosterTemplateInvalidResponseException(
                message = "QR Code poster template could not be parsed",
                cause = exception
            )
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun getTemplateFromApiOrCache(): ByteArray {
        return try {
            val response = api.getQrCodePosterTemplate()
            if (response.body() == null) {
                throw IllegalStateException("Response is successful, but body is empty.")
            }

            val fileMap = response.body()!!.byteStream().unzip().readIntoMap()

            val exportBinary = fileMap[EXPORT_BINARY_FILE_NAME]
            val exportSignature = fileMap[EXPORT_SIGNATURE_FILE_NAME]

            if (exportBinary == null || exportSignature == null) {
                throw QrCodePosterTemplateInvalidResponseException(message = "Unknown files: ${fileMap.keys}")
            }

            val hasValidSignature = signatureValidation.hasValidSignature(
                exportBinary,
                SignatureValidation.parseTEKStyleSignature(exportSignature)
            )

            if (!hasValidSignature) {
                throw QrCodePosterTemplateInvalidResponseException(message = "Invalid Signature!")
            }

            exportBinary
        } catch (exception: Exception) {
            Timber.d(exception, "Response is not successful, trying to load template from cache")
            defaultTemplateSource.getDefaultQrCodePosterTemplate()
        }
    }

    companion object {
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"
    }
}
