package de.rki.coronawarnapp.covidcertificate.signature.core

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DscMessage
import de.rki.coronawarnapp.server.protocols.internal.dgc.DscListOuterClass
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import javax.inject.Inject

@Suppress("MaxLineLength")
class DscSignatureValidatorTest : BaseTest() {

    @Inject lateinit var extractor: DccQrCodeExtractor

    @BeforeEach
    fun setup() {
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
    }

    private val dscData =
        "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"

    private val vaccinationCer =
        "HC1:6BF680-80T9WJWG.FKY*4GO0N4G7/3QHA2GUFBB853*70HS8FN0GMCGZSWY0UBCHCMD97TK0F90KECTHGWJC0FDQ%5AIA%G7X+AQB9746HS80:5%IBPB8646\$A8.BAG+97697:6WNAV+90Z9NH9I:6ZYA\$X9Y57UPC1JCWY8FVCBJ0LVC6JD846Y96C465W5EM6+EDXVET3E5\$CSUE6O9NPCSW5F/DBWENWE4WEB\$D% D3IA4W5646646E%6//6.JC\$9EU34..DZPC/EDIEC  C%W5RB88NASN8B+AKF6M-A0LEMW5: CQVDKQEBJ09WEQDD+Q6TW6FA7C466KCK9E2H9G:6V6BFM6GVC*JC1A6ZW63W5+/6846TPCBEC7ZKW.C3 C*ED*KE JC3/D0C8: CZEDZ CW.C7WEZM8EIAMC9I3D9WEDB8AY81C9XY8O/EZKEZ967L6156C68.97JME88KCNRJV9LL6JF01Q8+TMUDK6UK3DWBJA*/UINSZWD7WFIWIU/E5LK%QC\$:PMZE\$D94OTC7SE/LZXOUISZS6C57H8BB0HQ4"
    private val rc =
        "HC1:6BFOXN*TS0BI\$ZD8UHRTHZDE+VJG\$LM0IE3NAD61:OLX8:PT3G4/+62XGZHFPV5-FJLF6EB9UM97H98\$QJEQ8999Q9E\$BLZIA9JQ-JPEB8IIV1J2TS49BGIIZ0KZPIL3UBC3GIIC0JRPIU9T +TWZJ\$7K+ CUED:NK-9C5QDUBJ6+Q4U77Q4UYQD*O%+Q.SQBDO4C53752HPPEPHCR7XB3DON95U/3UEEZ.C3C9S/F\$JDCHHZ4FNLE72MC.BPC9SC95C9PG9AWBKHK6IA8B5C3DGZKCWCJZI+EBR3E%JTQOL2009UVD0HX2JR\$46N3:2V72SQ\$90:P2UK1\$PYZH-R3BU9JD09-N+\$RO0Q2E40TTKKVH*J+ VFUBCVC84WR-RC02BUCUETUSFEIESZGTLRD2TNJB0\$AKY8OWFB M6PUBMR+\$B+PPGQT24FSQCHBK.IFSEUR02I3K6ID-109-KY1"

    @Test
    fun validateSignature() {
        val dscList = DscListOuterClass.DscList.parseFrom(dscData.decodeBase64()!!.toByteArray())
        val dscData = DscData(
            dscList.certificatesList.map {
                DscItem(
                    it.kid.toOkioByteString().base64(),
                    it.data.toOkioByteString()
                )
            }
        )
        val dccData = extractor.extract(rc).data
        printBase64(dccData.dscMessage)
        DscSignatureValidator().validateSignature(dscData, dccData)
    }

    private fun printBase64(dscMessage: DscMessage) {
        Timber.d(
            """ 
                protectedHeader=${dscMessage.protectedHeader.base64()}
                signature=${dscMessage.signature.base64()}
                kid=${dscMessage.kid}
                payload=${dscMessage.payload.base64()}
                algorithm=${dscMessage.algorithm}
            """.trimIndent()
        )
    }
}
