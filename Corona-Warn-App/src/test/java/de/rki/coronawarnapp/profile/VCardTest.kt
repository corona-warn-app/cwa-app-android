package de.rki.coronawarnapp.profile

import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.profile.ui.qrcode.VCard
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.time.Instant
import java.time.format.ISODateTimeFormat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class VCardTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowJavaUTC } returns Instant.parse("1995-10-31T22:27:10Z")
    }

    @Test
    fun `Case 1`() {
        val profile = Profile(
            id = 1,
            firstName = "Max",
            lastName = "Mustermann",
            birthDate = ISODateTimeFormat.basicDate().parseLocalDate("19800625"),
            street = "Musterstrasse 14",
            zipCode = "51466",
            city = "Musterstadt",
            phone = "0190 1234567",
            email = "max@mustermann.de"
        )
        VCard(timeStamper).create(profile) shouldBe
            """
            BEGIN:VCARD
            VERSION:4.0
            N:Mustermann;Max;;;
            FN:Max Mustermann
            BDAY:19800625
            EMAIL;TYPE=home:max@mustermann.de
            TEL;TYPE="cell,home":0190 1234567
            ADR;TYPE=home:;;Musterstrasse 14;Musterstadt;;51466
            REV:19951031T222710Z
            END:VCARD
            """.trimIndent()
    }

    @Test
    fun `Case 2`() {
        val profile = Profile(
            id = 1,
            firstName = "",
            lastName = "",
            birthDate = null,
            street = "",
            zipCode = "",
            city = "",
            phone = "",
            email = ""
        )
        VCard(timeStamper).create(profile) shouldBe
            """
            BEGIN:VCARD
            VERSION:4.0
            N:;;;;
            FN:
            BDAY:
            EMAIL;TYPE=home:
            TEL;TYPE="cell,home":
            ADR;TYPE=home:;;;;;
            REV:19951031T222710Z
            END:VCARD
            """.trimIndent()
    }

    @Test
    fun `Case 3`() {
        val profile = Profile(
            id = 1,
            firstName = "Max",
            lastName = "Mustermann",
            birthDate = ISODateTimeFormat.basicDate().parseLocalDate("19800625"),
            street = "Mu\\ster;stra,sse 14",
            zipCode = "51466",
            city = "Musterstadt",
            phone = "0190 1234567",
            email = "max@mustermann.de"
        )
        VCard(timeStamper).create(profile) shouldBe
            """
            BEGIN:VCARD
            VERSION:4.0
            N:Mustermann;Max;;;
            FN:Max Mustermann
            BDAY:19800625
            EMAIL;TYPE=home:max@mustermann.de
            TEL;TYPE="cell,home":0190 1234567
            ADR;TYPE=home:;;Mu\\ster\;stra\,sse 14;Musterstadt;;51466
            REV:19951031T222710Z
            END:VCARD
            """.trimIndent()
    }

    @Test
    fun `Case 4`() {
        val profile = Profile(
            id = 1,
            firstName = "Max,",
            lastName = "Mustermann;",
            birthDate = ISODateTimeFormat.basicDate().parseLocalDate("19800625"),
            street = "Mu\\\\ster;stra,sse 14\nA",
            zipCode = "51466",
            city = "Muster city \n Upper \\county, DC ; US",
            phone = "0190 \\1234567",
            email = "max@mustermann,;\\.de"
        )
        VCard(timeStamper).create(profile) shouldBe
            """
            BEGIN:VCARD
            VERSION:4.0
            N:Mustermann\;;Max\,;;;
            FN:Max\, Mustermann\;
            BDAY:19800625
            EMAIL;TYPE=home:max@mustermann\,\;\\.de
            TEL;TYPE="cell,home":0190 \\1234567
            ADR;TYPE=home:;;Mu\\\\ster\;stra\,sse 14A;Muster city  Upper \\county\, DC \; US;;51466
            REV:19951031T222710Z
            END:VCARD
            """.trimIndent()
    }
}
