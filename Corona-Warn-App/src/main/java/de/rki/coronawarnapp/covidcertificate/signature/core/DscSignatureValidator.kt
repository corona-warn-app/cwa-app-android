package de.rki.coronawarnapp.covidcertificate.signature.core

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccCoseDecoder
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.covidcertificate.signature.core.exception.DscSignatureValidationException
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DscSignatureValidator @Inject constructor(
    private val dccCoseDecoder: DccCoseDecoder,
    private val dccQrCodeExtractor: DccQrCodeExtractor
) {

    /**
     *
     * @throws DscSignatureValidationException
     */
    suspend fun isSignatureValid(dscData: DscData, qrCodeString: QrCodeString): Boolean {
        Timber.tag(TAG).d("isSignatureValid(dscData=%s,certificateData=%s)", dscData, qrCodeString)
        val coseObject = dccQrCodeExtractor.extractCoseObject(qrCodeString)
        val dscMessage = dccCoseDecoder.decodeDscMessage(coseObject)

        return true
    }

    companion object {
        private const val TAG = "DscSignatureValidator"
    }
}
