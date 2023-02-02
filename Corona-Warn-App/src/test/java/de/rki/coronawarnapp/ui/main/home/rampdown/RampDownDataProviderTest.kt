package de.rki.coronawarnapp.ui.main.home.rampdown

import de.rki.coronawarnapp.ccl.configuration.storage.CclConfigurationRepository
import de.rki.coronawarnapp.ccl.rampdown.calculation.RampDownCalculation
import de.rki.coronawarnapp.ccl.rampdown.model.RampDownOutput
import de.rki.coronawarnapp.ccl.ui.text.CclTextFormatter
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class RampDownDataProviderTest : BaseTest() {

    @MockK lateinit var rampDownCalculation: RampDownCalculation
    @MockK lateinit var cclConfigurationRepository: CclConfigurationRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { rampDownCalculation.getStatusTabNotice(any()) } returns RampDownOutput(
            visible = false,
            titleText = null,
            subtitleText = null,
            faqAnchor = null,
            longText = null
        )

        every { cclConfigurationRepository.cclConfigurations } returns flowOf(emptyList())
    }

    @Test
    fun `getRampDownNotice - success`() = runTest {
        RampDownDataProvider(
            format = CclTextFormatter(
                cclJsonFunctions = mockk(),
                mapper = SerializationModule.jacksonBaseMapper
            ),
            rampDownCalculation = rampDownCalculation,
            cclConfigurationRepository = cclConfigurationRepository
        ).rampDownNotice.first() shouldBe RampDownNotice(
            visible = false,
            title = "",
            subtitle = "",
            description = "",
            faqUrl = null
        )
    }

    @Test
    fun `getRampDownNotice - error`() = runTest {
        coEvery {
            rampDownCalculation.getStatusTabNotice(any())
        } throws Exception("")

        RampDownDataProvider(
            format = CclTextFormatter(
                cclJsonFunctions = mockk(),
                mapper = SerializationModule.jacksonBaseMapper
            ),
            rampDownCalculation = rampDownCalculation,
            cclConfigurationRepository = cclConfigurationRepository
        ).rampDownNotice.first() shouldBe null
    }
}
