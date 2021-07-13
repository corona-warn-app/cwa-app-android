package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule.Description
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule.Type
import de.rki.coronawarnapp.covidcertificate.validation.core.server.DccValidationServer
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.assertions.throwables.shouldThrow
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
                {
                    "Type": "Acceptance",
                    "Logic": {
                        "!": [
                            {
                                "var": "payload.v.1"
                            }
                        ]
                    },
                    "Engine": "CERTLOGIC",
                    "Country": "LT",
                    "ValidTo": "2023-07-04T00:00:00Z",
                    "Version": "1.0.0",
                    "ValidFrom": "2021-07-04T15:00:00Z",
                    "Identifier": "VR-LT-0000",
                    "Description": [
                        {
                            "desc": "One type of event of vaccination",
                            "lang": "en"
                        }
                    ],
                    "EngineVersion": "1.0.0",
                    "SchemaVersion": "1.0.0",
                    "AffectedFields": [
                        "v.1"
                    ],
                    "CertificateType": "Vaccination"
                }
            ]
        """.trimIndent()
    private val testInvalidationRulesData =
        """
            [
                {
                    "Type": "Invalidation",
                    "Logic": {
                        "!": [
                            {
                                "var": "payload.v.1"
                            }
                        ]
                    },
                    "Engine": "CERTLOGIC",
                    "Country": "LT",
                    "ValidTo": "2023-07-04T00:00:00Z",
                    "Version": "1.0.0",
                    "ValidFrom": "2021-07-04T15:00:00Z",
                    "Identifier": "IR-DE-0000",
                    "Description": [
                        {
                            "desc": "One type of event of vaccination",
                            "lang": "en"
                        }
                    ],
                    "EngineVersion": "1.0.0",
                    "SchemaVersion": "1.0.0",
                    "AffectedFields": [
                        "v.1"
                    ],
                    "CertificateType": "Vaccination"
                }
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
            coEvery { ruleSetJson(Type.ACCEPTANCE) } returns testAcceptanceRulesData
            coEvery { ruleSetJson(Type.INVALIDATION) } returns testInvalidationRulesData
        }
    }

    private val serializationModule = SerializationModule()
    private val baseGson = serializationModule.baseGson()
    private val objectMapper = serializationModule.jacksonObjectMapper()

    private fun createInstance(scope: CoroutineScope) = DccValidationRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        gson = baseGson,
        server = server,
        localCache = localCache,
    )

    @Test
    fun `local cache is loaded on init - no server requests`() = runBlockingTest2(ignoreActive = true) {
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
            server.ruleSetJson(Type.ACCEPTANCE)
        }

        coVerify {
            localCache.loadInvalidationRuleJson()
        }
        coVerify(exactly = 0) {
            server.ruleSetJson(Type.INVALIDATION)
        }
    }

    @Test
    fun `refresh talks to server and updates local cache`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).apply {
            refresh()
            dccCountries.first() shouldBe listOf(
                DccCountry("DE"), DccCountry("NL")
            )
            acceptanceRules.first() shouldBe listOf(
                DccValidationRule(
                    identifier = "VR-LT-0000",
                    typeDcc = Type.ACCEPTANCE,
                    country = "LT",
                    version = "1.0.0",
                    schemaVersion = "1.0.0",
                    engine = "CERTLOGIC",
                    engineVersion = "1.0.0",
                    certificateType = "Vaccination",
                    description = listOf(Description("en", "One type of event of vaccination")),
                    validFrom = "2021-07-04T15:00:00Z",
                    validTo = "2023-07-04T00:00:00Z",
                    affectedFields = listOf("v.1"),
                    logic = objectMapper.readTree("{\"!\":[{\"var\":\"payload.v.1\"}]}")
                )
            )
            invalidationRules.first() shouldBe listOf(
                DccValidationRule(
                    identifier = "IR-DE-0000",
                    typeDcc = Type.INVALIDATION,
                    country = "LT",
                    version = "1.0.0",
                    schemaVersion = "1.0.0",
                    engine = "CERTLOGIC",
                    engineVersion = "1.0.0",
                    certificateType = "Vaccination",
                    description = listOf(Description("en", "One type of event of vaccination")),
                    validFrom = "2021-07-04T15:00:00Z",
                    validTo = "2023-07-04T00:00:00Z",
                    affectedFields = listOf("v.1"),
                    logic = objectMapper.readTree("{\"!\":[{\"var\":\"payload.v.1\"}]}")
                )
            )
        }

        coVerify {
            server.dccCountryJson()
            localCache.saveCountryJson(testCountryData)
            server.ruleSetJson(Type.ACCEPTANCE)
            localCache.saveAcceptanceRulesJson(testAcceptanceRulesData)
            server.ruleSetJson(Type.INVALIDATION)
            localCache.saveInvalidationRulesJson(testInvalidationRulesData)
        }
    }

    @Test
    fun `bad acceptance rules yields exception`() = runBlockingTest2(ignoreActive = true) {
        // Missing attributes
        coEvery { server.ruleSetJson(Type.ACCEPTANCE) } returns """
            [
                {
                    "Type": "Acceptance",
                    "Engine": "CERTLOGIC",
                    "Country": "LT",
                    "Version": "1.0.0",
                    "Identifier": "VR-LT-0000",
                    "EngineVersion": "1.0.0",
                    "SchemaVersion": "1.0.0",
                }
            ]
        """.trimIndent()

        val instance = createInstance(this)

        shouldThrow<DccValidationException> {
            instance.refresh()
        }.errorCode shouldBe DccValidationException.ErrorCode.ACCEPTANCE_RULE_JSON_DECODING_FAILED

        coVerify {
            server.dccCountryJson()
            localCache.saveCountryJson(testCountryData)
            server.ruleSetJson(Type.ACCEPTANCE)
        }
        coVerify(exactly = 0) {
            localCache.saveAcceptanceRulesJson(any())
            server.ruleSetJson(Type.INVALIDATION)
            localCache.saveInvalidationRulesJson(any())
        }
    }

    @Test
    fun `bad invaldation rules yields exception`() = runBlockingTest2(ignoreActive = true) {
        // Missing attributes
        coEvery { server.ruleSetJson(Type.INVALIDATION) } returns """
            [
                {
                    "Type": "Invalidation",
                    "Engine": "CERTLOGIC",
                    "Country": "LT",
                    "Version": "1.0.0",
                    "Identifier": "VR-LT-0000",
                    "EngineVersion": "1.0.0",
                    "SchemaVersion": "1.0.0",
                }
            ]
        """.trimIndent()
        shouldThrow<DccValidationException> {
            createInstance(this).refresh()
        }.errorCode shouldBe DccValidationException.ErrorCode.INVALIDATION_RULE_JSON_DECODING_FAILED

        coVerify {
            server.dccCountryJson()
            localCache.saveCountryJson(testCountryData)
            server.ruleSetJson(Type.ACCEPTANCE)
            localCache.saveAcceptanceRulesJson(testAcceptanceRulesData)
            server.ruleSetJson(Type.INVALIDATION)
        }
        coVerify(exactly = 0) {
            localCache.saveInvalidationRulesJson(any())
        }
    }
}
