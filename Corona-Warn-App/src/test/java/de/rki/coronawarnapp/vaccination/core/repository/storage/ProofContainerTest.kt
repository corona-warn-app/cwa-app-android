package de.rki.coronawarnapp.vaccination.core.repository.storage

import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import de.rki.coronawarnapp.vaccination.core.VaccinationTestData
import io.kotest.matchers.shouldBe
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ProofContainerTest : BaseTest() {

    private val storedProof = VaccinationTestData.PERSON_A_VAC_1_CERT

    private val container = VaccinationTestData.PERSON_A_VAC_1_CONTAINER

    @Test
    fun `person identifier calculation`() {
        container.personIdentifier shouldBe VaccinatedPersonIdentifier(
            firstNameStandardized = "FRANCOIS<JOAN",
            lastNameStandardized = "DARSONS<VAN<HALEN",
            dateOfBirth = LocalDate.parse("2009-02-28"),
        )

        container.personIdentifier.code shouldBe "2009-02-28#DARSONS<VAN<HALEN#FRANCOIS<JOAN"

        container.copy(
            certificate = storedProof.copy(firstNameStandardized = " ")
        ).personIdentifier.code shouldBe "2009-02-28#DARSONS<VAN<HALEN# "

        container.copy(
            certificate = storedProof.copy(firstNameStandardized = "")
        ).personIdentifier.code shouldBe "2009-02-28#DARSONS<VAN<HALEN#"
    }
}
