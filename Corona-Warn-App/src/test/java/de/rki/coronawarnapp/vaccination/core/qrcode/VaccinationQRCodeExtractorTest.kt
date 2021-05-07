package de.rki.coronawarnapp.vaccination.core.qrcode

import de.rki.coronawarnapp.vaccination.decoder.Base45Decoder
import de.rki.coronawarnapp.vaccination.decoder.COSEDecoder
import de.rki.coronawarnapp.vaccination.decoder.ZLIBDecompressor
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class VaccinationQRCodeExtractorTest : BaseTest() {

    private val base45Decoder = Base45Decoder()
    private val ZLIBDecompressor = ZLIBDecompressor()
    private val coseDecoder = COSEDecoder()
    private val CBORDecoder = VaccinationCertificateV1Decoder()

    @Test
    fun `happy path extraction`() {
        VaccinationQRCodeExtractor(
            base45Decoder,
            ZLIBDecompressor,
            coseDecoder,
            CBORDecoder
        ).extract(TestData.validVaccinationQrCode)
    }
}
