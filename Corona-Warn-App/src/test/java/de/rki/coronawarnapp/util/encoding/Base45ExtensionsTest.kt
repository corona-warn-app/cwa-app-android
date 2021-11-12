package de.rki.coronawarnapp.util.encoding

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class Base45ExtensionsTest : BaseTest() {

    private val bytesToEncodedString = mapOf(
        ByteArray(0) { 0 } to "",
        ByteArray(1) { 0 } to "00",
        "AB".toByteArray() to "BB8",
        "Hello!!".toByteArray() to "%69 VD92EX0",
        "base-45".toByteArray() to "UJCLQE7W581",
        ByteArray(5) { 0 } to "00000000",
        ByteArray(7) { -1 } to "FGWFGWFGWU5",
        ByteArray(4 * 256) { (it % 256).toUByte().toByte() } to (
            "100KB0*M0DY0W016C1PN1:Y1I12\$C2BO2UZ2423ND3.O3G 3Z249E4SP42\$4L35+E5EQ5X\$5746QF60R6J%6%47CG7VR75*" +
                "7O58/G8HS8 *8A69TH93T9M+9-6AFIAYTA8-AR7B1JBKUB*-BD8CWJC6VCP.C:8DIKD\$VDB/DU9E4LENWE./EGAFZLF9XFS:F2" +
                "BGLMG+XGE0HXBH7NHQYH01IJCI%NICZIV1J5DJOOJ/ZJH2K DKAPKT K33LMEL-PLF\$LY3M8FMRQM1%MK4N*FNDRNW%N65OPGO" +
                ":ROI*O\$5PBHPUSP4+PN6Q.HQGTQZ+Q97RSIR2URL-R+7SEJSXUS7.SQ8T0KTJVT%.TC9UVKU5WUO/U/9VHLV WVA:VTAW"
            ).repeat(4),
        "\uD83E\uDDB8\uD83C\uDFFF\u200D♀️".toByteArray() to "*IUK3L*IUY7IOSS7.HBIJXDU83"
    )

    @Test
    fun `encode - extension`() {
        bytesToEncodedString.forEach {
            it.key.base45() shouldBe it.value
        }
    }

    @Test
    fun `decode - extension`() {
        bytesToEncodedString.forEach {
            it.value.decodeBase45().bytes shouldBe it.key
        }
    }

    @Test
    fun `decode - failures`() {
        "Äß".decodeBase45() shouldBe DecodingFailure("Illegal Base45 character (Ä)!")
        "0000".decodeBase45() shouldBe DecodingFailure("Illegal length of Base45 string (4)!")
    }
}
