package de.rki.coronawarnapp.covidcertificate.booster

import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Lazy
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.util.serialization.BaseJackson
import dgca.verifier.app.engine.DefaultCertLogicEngine
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class DccBoosterRulesValidatorTest : BaseTest() {

    @Inject lateinit var engine: Lazy<DefaultCertLogicEngine>
    @Inject @BaseJackson lateinit var objectMapper: ObjectMapper
    @MockK lateinit var dccBoosterRulesRepository: BoosterRulesRepository

    @BeforeEach
    fun setUp() {
        DaggerCovidCertificateTestComponent.create().inject(this)
        MockKAnnotations.init(this)
        coEvery { dccBoosterRulesRepository.rules } returns flowOf(emptyList())
    }

    @Test
    fun `Empty BoosterRules returns null`() = runBlockingTest {
        val mock = mockk<VaccinationCertificate>()
        validator().validateBoosterRules(listOf(mock)) shouldBe null
    }

    @Test
    fun `Empty Certificates List returns null`() = runBlockingTest {
        val mockRule = mockk<DccValidationRule>()
        coEvery { dccBoosterRulesRepository.rules } returns flowOf(listOf(mockRule))

        validator().validateBoosterRules(emptyList()) shouldBe null
    }

    private fun validator() = DccBoosterRulesValidator(
        boosterRulesRepository = dccBoosterRulesRepository,
        engine = engine,
        objectMapper = objectMapper
    )
}
