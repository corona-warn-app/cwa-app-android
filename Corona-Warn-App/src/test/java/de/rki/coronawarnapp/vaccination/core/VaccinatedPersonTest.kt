package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.vaccination.core.repository.storage.PersonData
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationContainer
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class VaccinatedPersonTest : BaseTest() {

    @Test
    fun `test name combinations`() {
        val certificate = mockk<VaccinationCertificate>()
        val vaccinationContainer = mockk<VaccinationContainer>().apply {
            every { toVaccinationCertificate(any()) } returns certificate
        }
        val personData = mockk<PersonData>().apply {
            every { vaccinations } returns setOf(vaccinationContainer)
        }
        val vaccinatedPerson = VaccinatedPerson(
            data = personData,
            valueSet = null
        )

        certificate.apply {
            every { firstName } returns "Straw"
            every { lastName } returns "Berry"
        }
        vaccinatedPerson.fullName shouldBe "Straw Berry"

        certificate.apply {
            every { firstName } returns null // Thermo
            every { lastName } returns "Siphon"
        }
        vaccinatedPerson.fullName shouldBe "Siphon"
    }
}
