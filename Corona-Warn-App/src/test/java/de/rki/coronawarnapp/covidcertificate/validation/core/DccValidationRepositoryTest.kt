package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.server.DccValidationServer
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2

class DccValidationRepositoryTest : BaseTest() {
    @MockK lateinit var server: DccValidationServer
    @MockK lateinit var localCache: DccValidationCache

    private val testCountryData = "[\"DE\",\"NL\"]"
    private val testAcceptanceRulesData =
        """
            [
            ]
        """.trimIndent()
    private val testInvalidationRulesData =
        """
            [
            ]
        """.trimIndent()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        localCache.apply {
            coEvery { loadCountryJson() } returns null
            coEvery { saveCountryJson(any()) } just Runs
            coEvery { loadAcceptanceRuleJson() } returns null
            coEvery { saveAcceptanceRulesJson(any()) } just Runs
            coEvery { loadInvalidationRuleJson() } returns null
            coEvery { saveInvalidationRulesJson(any()) } just Runs
        }

        server.apply {
            coEvery { dccCountryJson() } returns testCountryData
            coEvery { ruleSetJson(DccValidationRule.Type.ACCEPTANCE) } returns testAcceptanceRulesData
            coEvery { ruleSetJson(DccValidationRule.Type.INVALIDATION) } returns testInvalidationRulesData
        }
    }

    private fun createInstance(scope: CoroutineScope) = DccValidationRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        gson = SerializationModule().baseGson(),
        server = server,
        localCache = localCache,
    )

    @Test
    fun `local cache is loaded on init - no server request`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).apply {
            dccCountries.first() shouldBe emptyList()
            acceptanceRules.first() shouldBe emptyList()
            invalidationRules.first() shouldBe emptyList()
        }

        coVerify {
            localCache.loadCountryJson()
        }
        coVerify(exactly = 0) {
            server.dccCountryJson()
        }

        coVerify {
            localCache.loadAcceptanceRuleJson()
        }
        coVerify(exactly = 0) {
            server.ruleSetJson(DccValidationRule.Type.ACCEPTANCE)
        }

        coVerify {
            localCache.loadInvalidationRuleJson()
        }
        coVerify(exactly = 0) {
            server.ruleSetJson(DccValidationRule.Type.INVALIDATION)
        }
    }

    @Test
    fun `refresh talks to server and updates local cache`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).apply {
            refresh()
            dccCountries.first() shouldBe listOf(
                DccCountry("DE"), DccCountry("NL")
            )
        }

        coVerify {
            server.dccCountryJson()
            localCache.saveCountryJson(testCountryData)
            server.ruleSetJson(DccValidationRule.Type.ACCEPTANCE)
            localCache.saveAcceptanceRulesJson(testAcceptanceRulesData)
            server.ruleSetJson(DccValidationRule.Type.INVALIDATION)
            localCache.saveAcceptanceRulesJson(testInvalidationRulesData)
        }
    }
}
