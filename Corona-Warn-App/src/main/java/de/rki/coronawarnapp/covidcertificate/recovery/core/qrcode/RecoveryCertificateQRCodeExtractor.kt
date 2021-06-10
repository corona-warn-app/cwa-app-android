package de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccCoseDecoder
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccHeaderParser
import de.rki.coronawarnapp.covidcertificate.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.covidcertificate.recovery.core.certificate.RecoveryDccParser
import de.rki.coronawarnapp.covidcertificate.recovery.core.certificate.RecoveryDccV1
import javax.inject.Inject

@Reusable
class RecoveryCertificateQRCodeExtractor @Inject constructor(
    private val coseDecoder: DccCoseDecoder,
    private val headerParser: DccHeaderParser,
    private val bodyParser: RecoveryDccParser,
) {

    /**
     * May throw an **[InvalidTestCertificateException]**
     */
    fun extract(qrCode: String) = RecoveryCertificateQRCode(
        data = qrCode.extract(),
        qrCode = qrCode
    )

    private fun String.extract(): DccData<RecoveryDccV1> = throw NotImplementedError()
}
