package de.rki.coronawarnapp.ccl.holder.grouping

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.util.dcc.cleanHolderName
import de.rki.coronawarnapp.util.dcc.group
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTestInstrumentation

class DccHolderComparisonTest : BaseTestInstrumentation() {

    @ParameterizedTest(name = "{index}: {0}")
    @ArgumentsSource(DccHolderComparisonTestCaseProvider::class)
    fun allTestCases(testCase: TestCase) {
        val mapper = SerializationModule().jacksonObjectMapper()
        println("Executing TestCase: ${mapper.writerWithDefaultPrettyPrinter().writeValueAsString(testCase)}")

        val certA: CwaCovidCertificate = mockk {
            every { dateOfBirthFormatted } returns testCase.holderA.dateOfBirth
            every { firstName } returns testCase.holderA.name.givenName
            every { lastName } returns testCase.holderA.name.familyName
        }

        val certB: CwaCovidCertificate = mockk {
            every { dateOfBirthFormatted } returns testCase.holderB.dateOfBirth
            every { firstName } returns testCase.holderB.name.givenName
            every { lastName } returns testCase.holderB.name.familyName
        }

        val result = setOf(certA, certB).group()

        println(result)

        if(testCase.isEqual) result.count() shouldBe 1 else result.count() shouldBe 2
    }
}

fun compare(holderA: Holder, holderB: Holder): Boolean {
    if (holderA.dateOfBirth.trim() != holderB.dateOfBirth.trim()) return false

    val firstNameA = holderA.name.givenName.cleanHolderName()
    val firstNameB = holderB.name.givenName.cleanHolderName()
    val firstNameMatch = firstNameA.intersect(firstNameB).isNotEmpty()

    val lastNameA = holderA.name.familyName.cleanHolderName()
    val lastNameB = holderB.name.familyName.cleanHolderName()
    val lastNameMatch = lastNameA.intersect(lastNameB).isNotEmpty()

    if (firstNameMatch && lastNameMatch) return true

    return firstNameB.intersect(lastNameA).isNotEmpty() && firstNameA.intersect(lastNameB).isNotEmpty()
}

data class TestCases(
    @JsonProperty("\$comment")
    val comment: String,

    @JsonProperty("\$sourceHash")
    val sourceHash: String,

    @JsonProperty("data")
    val testCases: List<TestCase>
)

data class TestCase(
    @JsonProperty("description")
    val description: String,

    @JsonProperty("actHolderA")
    val holderA: Holder,

    @JsonProperty("actHolderB")
    val holderB: Holder,

    @JsonProperty("expIsSameHolder")
    val isEqual: Boolean
)

data class Holder(
    @JsonProperty("nam")
    val name: HolderName,

    @JsonProperty("dob")
    val dateOfBirth: String
)

data class HolderName(
    @JsonProperty("gnt")
    val givenName: String,

    @JsonProperty("fnt")
    val familyName: String
)
