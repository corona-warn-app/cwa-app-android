package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ValueSetWrapperTest : BaseTest() {

    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    private lateinit var valueSetWrapper: ValueSetWrapper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { valueSetsRepository.latestVaccinationValueSets } returns
            flowOf(ValueSetTestData.vaccinationValueSetsEn)
        coEvery { valueSetsRepository.latestTestCertificateValueSets } returns
            flowOf(ValueSetTestData.testCertificateValueSetsEn)
        valueSetWrapper = ValueSetWrapper(valueSetsRepository)
    }

    @Test
    fun `recovery value set`() = runBlockingTest {
        valueSetWrapper.valueSetRecovery.first() shouldBe mapOf(
            countryCodes,
            "disease-agent-targeted" to listOf(ValueSetTestData.tgItemEn.first),
        )
    }

    @Test
    fun `vaccination value set`() = runBlockingTest {
        valueSetWrapper.valueSetVaccination.first() shouldBe mapOf(
            countryCodes,
            "disease-agent-targeted" to listOf(ValueSetTestData.tgItemEn.first),
            "sct-vaccines-covid-19" to listOf(ValueSetTestData.vpItemEn.first),
            "vaccines-covid-19-auth-holders " to listOf(ValueSetTestData.maItemEn.first),
            "vaccines-covid-19-names" to listOf(ValueSetTestData.mpItemEn.first),
        )
    }

    @Test
    fun `test value set`() = runBlockingTest {
        valueSetWrapper.valueSetTest.first() shouldBe mapOf(
            countryCodes,
            "disease-agent-targeted" to listOf(ValueSetTestData.tgItemEn.first),
            "covid-19-lab-result" to listOf(ValueSetTestData.trItemEn.first),
            "covid-19-lab-test-manufacturer-and-name" to listOf(ValueSetTestData.tcMaItemEn.first),
            "covid-19-lab-test-type" to listOf(ValueSetTestData.ttItemEn.first),
        )
    }
}
