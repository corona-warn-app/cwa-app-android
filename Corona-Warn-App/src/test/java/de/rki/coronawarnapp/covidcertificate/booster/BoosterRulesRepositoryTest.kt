package de.rki.coronawarnapp.covidcertificate.booster

import de.rki.coronawarnapp.ccl.configuration.storage.BoosterRulesStorage
import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule.Type
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRuleConverter
import de.rki.coronawarnapp.covidcertificate.validation.core.server.DccValidationServer
import de.rki.coronawarnapp.util.repositories.UpdateResult
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runTest2

class BoosterRulesRepositoryTest : BaseTest() {

    @MockK lateinit var server: DccValidationServer
    @MockK lateinit var storage: BoosterRulesStorage

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

    private val testBoosterNotificationRulesServerResult = DccValidationServer.RuleSetResult(
        ruleSetJson = testBoosterNotificationRulesData,
        source = DccValidationServer.RuleSetSource.SERVER
    )

    private val testBoosterNotificationRulesCacheResult = DccValidationServer.RuleSetResult(
        ruleSetJson = testBoosterNotificationRulesData,
        source = DccValidationServer.RuleSetSource.CACHE
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        storage.apply {
            coEvery { loadBoosterRulesJson() } returns null
            coEvery { saveBoosterRulesJson(any()) } just runs
        }

        server.apply {
            coEvery { ruleSetJson(Type.BOOSTER_NOTIFICATION) } returns testBoosterNotificationRulesServerResult
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
        storage = storage,
    )

    @Test
    fun `update booster notification rules server fails and no cache`() = runTest2 {
        coEvery {
            server.ruleSetJson(Type.BOOSTER_NOTIFICATION)
        } throws DccValidationException(DccValidationException.ErrorCode.BOOSTER_NOTIFICATION_RULE_SERVER_ERROR)

        with(createInstance(this)) {
            update() shouldBe UpdateResult.FAIL
            rules.first() shouldBe emptyList()
        }

        coVerify {
            server.ruleSetJson(Type.BOOSTER_NOTIFICATION)
        }

        coVerify(exactly = 0) {
            storage.saveBoosterRulesJson(any())
        }
    }

    @Test
    fun `update booster notification rules success`() = runTest2 {
        val boosterRuleList = listOf(testBoosterNotificationRule)

        coEvery { server.ruleSetJson(Type.BOOSTER_NOTIFICATION) } returns testBoosterNotificationRulesServerResult

        with(createInstance(this)) {
            update() shouldBe UpdateResult.UPDATE
            rules.first() shouldBe boosterRuleList
        }

        coVerify {
            server.ruleSetJson(Type.BOOSTER_NOTIFICATION)
            storage.saveBoosterRulesJson(any())
        }
    }

    @Test
    fun `update booster notification - no new rules - getting data from cache`() =
        runTest2 {
            val boosterRuleList = listOf(testBoosterNotificationRule)

            coEvery { server.ruleSetJson(Type.BOOSTER_NOTIFICATION) } returns testBoosterNotificationRulesCacheResult

            with(createInstance(this)) {
                update() shouldBe UpdateResult.NO_UPDATE
                rules.first() shouldBe boosterRuleList
            }

            coVerify {
                server.ruleSetJson(Type.BOOSTER_NOTIFICATION)
                storage.loadBoosterRulesJson()
            }

            coVerify(exactly = 0) {
                storage.saveBoosterRulesJson(any())
            }
        }

    @Test
    fun `bad booster notification rules do not wreck cache`() = runTest2 {
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

        coEvery { storage.loadBoosterRulesJson() } returns testBoosterNotificationRulesData

        val boosterRuleList = listOf(testBoosterNotificationRule)

        with(createInstance(this)) {
            update() shouldBe UpdateResult.FAIL
            rules.first() shouldBe boosterRuleList
        }

        coVerify {
            server.ruleSetJson(Type.BOOSTER_NOTIFICATION)
        }

        coVerify(exactly = 0) {
            storage.saveBoosterRulesJson(any())
        }
    }

    @Test
    fun `reset clears flow`() = runTest2 {
        val bnrs = listOf(testBoosterNotificationRule)
        createInstance(this).run {
            update() shouldBe UpdateResult.UPDATE
            rules.first() shouldBe bnrs

            reset()

            rules.first() shouldBe emptyList()
        }
    }
}
