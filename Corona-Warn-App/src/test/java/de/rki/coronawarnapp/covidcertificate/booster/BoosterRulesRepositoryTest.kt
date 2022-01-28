package de.rki.coronawarnapp.covidcertificate.booster

import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationCache
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule.Type
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRuleConverter
import de.rki.coronawarnapp.covidcertificate.validation.core.server.DccValidationServer
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2

class BoosterRulesRepositoryTest : BaseTest() {

    @MockK lateinit var server: DccValidationServer
    @MockK lateinit var localCache: DccValidationCache

    private val testBoosterNotificationRulesData =
        """
            [
                {
                    "Type": "BoosterNotification",
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

    val testBoosterNotificationRulesResult = DccValidationServer.RuleSetResult(
        ruleSetJson = testBoosterNotificationRulesData,
        source = DccValidationServer.RuleSetSource.SERVER
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        localCache.apply {
            coEvery { loadBoosterNotificationRulesJson() } returns null
            coEvery { saveBoosterNotificationRulesJson(any()) } just runs
        }

        server.apply {
            coEvery { ruleSetJson(Type.BOOSTER_NOTIFICATION) } returns testBoosterNotificationRulesResult
            every { clear() } just runs
        }
    }

    private val serializationModule = SerializationModule()
    private val baseGson = serializationModule.baseGson()
    private val objectMapper = serializationModule.jacksonObjectMapper()
    private val converter = DccValidationRuleConverter(baseGson)

    private val testBoosterNotificationRule = DccValidationRule(
        identifier = "IR-DE-0000",
        typeDcc = Type.BOOSTER_NOTIFICATION,
        country = "LT",
        version = "1.0.0",
        schemaVersion = "1.0.0",
        engine = "CERTLOGIC",
        engineVersion = "1.0.0",
        certificateType = "Vaccination",
        description = listOf(DccValidationRule.Description("en", "One type of event of vaccination")),
        validFrom = "2021-07-04T15:00:00Z",
        validTo = "2023-07-04T00:00:00Z",
        affectedFields = listOf("v.1"),
        logic = objectMapper.readTree("{\"!\":[{\"var\":\"payload.v.1\"}]}")
    )

    private fun createInstance(scope: CoroutineScope) = BoosterRulesRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        converter = converter,
        server = server,
        localCache = localCache
    )

    @Test
    fun `update booster notification rules server fails and no cache`() = runBlockingTest2(ignoreActive = true) {
        coEvery {
            server.ruleSetJson(Type.BOOSTER_NOTIFICATION)
        } throws DccValidationException(DccValidationException.ErrorCode.BOOSTER_NOTIFICATION_RULE_SERVER_ERROR)

        with(createInstance(this)) {
            update() shouldBe false
            rules.first() shouldBe emptyList()
        }

        coVerify {
            server.ruleSetJson(Type.BOOSTER_NOTIFICATION)
        }

        coVerify(exactly = 0) {
            localCache.saveBoosterNotificationRulesJson(any())
        }
    }

    @Test
    fun `update booster notification rules success`() = runBlockingTest2(ignoreActive = true) {
        val boosterRuleList = listOf(testBoosterNotificationRule)

        coEvery { server.ruleSetJson(Type.BOOSTER_NOTIFICATION) } returns testBoosterNotificationRulesResult

        with(createInstance(this)) {
            update() shouldBe boosterRuleList
            rules.first() shouldBe boosterRuleList
        }

        coVerify {
            server.ruleSetJson(Type.BOOSTER_NOTIFICATION)
            localCache.saveBoosterNotificationRulesJson(any())
        }
    }

    @Test
    fun `bad booster notification rules do not wreck cache`() = runBlockingTest2(ignoreActive = true) {
        // Missing attributes
        coEvery { server.ruleSetJson(Type.BOOSTER_NOTIFICATION) } returns DccValidationServer.RuleSetResult(
            ruleSetJson = """
            [
                {
                    "Type": "BoosterNotification",
                    "Engine": "CERTLOGIC",
                    "Country": "LT",
                    "Version": "1.0.0",
                    "Identifier": "VR-LT-0000",
                    "EngineVersion": "1.0.0",
                    "SchemaVersion": "1.0.0",
                }
            ]
            """.trimIndent(),
            source = DccValidationServer.RuleSetSource.SERVER
        )

        coEvery { localCache.loadBoosterNotificationRulesJson() } returns testBoosterNotificationRulesData

        val boosterRuleList = listOf(testBoosterNotificationRule)

        with(createInstance(this)) {
            update() shouldBe boosterRuleList
            rules.first() shouldBe boosterRuleList
        }

        coVerify {
            server.ruleSetJson(Type.BOOSTER_NOTIFICATION)
        }

        coVerify(exactly = 0) {
            localCache.saveBoosterNotificationRulesJson(any())
        }
    }

    @Test
    fun `clear clears server, cache and flow`() = runBlockingTest2(ignoreActive = true) {
        val bnrs = listOf(testBoosterNotificationRule)
        createInstance(this).run {
            update() shouldBe bnrs
            rules.first() shouldBe bnrs

            clear()

            rules.first() shouldBe emptyList()
        }

        coVerify {
            server.clear()
            localCache.saveBoosterNotificationRulesJson(null)
        }
    }
}
