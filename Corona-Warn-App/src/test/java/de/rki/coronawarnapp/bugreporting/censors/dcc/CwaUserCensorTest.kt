package de.rki.coronawarnapp.bugreporting.censors.dcc

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

@Suppress("MaxLineLength")
class CwaUserCensorTest : BaseTest() {

    @MockK lateinit var personCertificatesSettings: PersonCertificatesSettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createInstance() = CwaUserCensor(personCertificatesSettings)

    @Test
    fun `censoring of certificate person identifier works`() = runBlockingTest {

        every { personCertificatesSettings.currentCwaUser } returns flowOf(
            CertificatePersonIdentifier(
                "1982-02-11",
                "LIME",
                "MOTHER<MANDY"
            )
        )

        val censor = createInstance()
        val certDataToCensor =
            "PersonCertificatesProvider: vaccPersons=[], tests=[], recos=[], cwaUser=CertificatePersonIdentifier(dateOfBirthFormatted=1982-02-11, lastNameStandardized=LIME, firstNameStandardized=MOTHER<MANDY)"
        censor.checkLog(certDataToCensor)!!
            .compile()!!.censored shouldBe "PersonCertificatesProvider: vaccPersons=[], tests=[], recos=[], cwaUser=CertificatePersonIdentifier(dateOfBirthFormatted=cwaUser/dateOfBirth, lastNameStandardized=cwaUser/lastNameStandardized, firstNameStandardized=cwaUser/firstNameStandardized)"
    }

    @Test
    fun `checkLog() should return null if nothing should be censored`() = runBlockingTest {
        every { personCertificatesSettings.currentCwaUser } returns flowOf(null)

        val censor = createInstance()
        val certDataToCensor = "Nothing interesting here"
        censor.checkLog(certDataToCensor) shouldBe null
    }
}
