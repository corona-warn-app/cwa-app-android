package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import com.fasterxml.jackson.databind.node.TextNode
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.ccl.configuration.model.FunctionDefinition
import de.rki.coronawarnapp.ccl.configuration.model.FunctionParameter
import de.rki.coronawarnapp.ccl.configuration.model.JsonFunctionsDescriptor
import de.rki.coronawarnapp.ccl.configuration.storage.CCLConfigurationRepository
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.jfn.error.NoSuchFunctionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class CclJsonFunctionsTest : BaseTest() {

    private val param1 = FunctionParameter(
        name = "greeting",
        default = TextNode.valueOf("Hello")
    )

    private val param2 = FunctionParameter(
        name = "name"
    )

    private val jfnLogic = SerializationModule.jacksonBaseMapper.readTree(
        """
            {
              "return": [
                {
                  "concatenate": [
                    {
                      "var": "greeting"
                    },
                    " ",
                    {
                      "var": "name"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
    )

    private val param = SerializationModule.jacksonBaseMapper.readTree(
        """
            {"name":"Android!"}
        """.trimIndent()
    )

    private val jfnDescriptor = JsonFunctionsDescriptor(
        name = "greet",
        definition = FunctionDefinition(
            parameters = listOf(param1, param2),
            logic = listOf(jfnLogic)
        )
    )

    private val cclConfiguration = CCLConfiguration(
        identifier = "CCL-DE-0001",
        type = CCLConfiguration.Type.CCLConfiguration,
        country = "DE",
        version = "1.0.0",
        schemaVersion = "1.0.0",
        engine = "JsonFunctions",
        engineVersion = "1.0.0",
        _validFrom = "2021-10-07T00:00:00Z",
        _validTo = "2030-06-01T00:00:00Z",
        logic = CCLConfiguration.Logic(jfnDescriptors = listOf(jfnDescriptor))
    )

    @MockK private lateinit var cclConfigurationRepository: CCLConfigurationRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { cclConfigurationRepository.getCCLConfigurations() } returns listOf(cclConfiguration)
    }

    @Test
    fun update() = runBlockingTest {
        val jfnDescriptor = JsonFunctionsDescriptor(
            name = "sayHi",
            definition = FunctionDefinition(
                parameters = listOf(param1, param2),
                logic = listOf(jfnLogic)
            )
        )
        val newConfig = cclConfiguration.copy(logic = CCLConfiguration.Logic(jfnDescriptors = listOf(jfnDescriptor)))
        instance(scope = this).apply {
            update(listOf(newConfig))
            evaluateFunction("sayHi", param).asText() shouldBe "Hello Android!"
            shouldThrow<NoSuchFunctionException> {
                evaluateFunction("greet", param)
            }.printStackTrace()
        }
    }

    @Test
    fun evaluateFunction() = runBlockingTest {
        instance(this).apply {
            evaluateFunction("greet", param).asText() shouldBe "Hello Android!"
            shouldThrow<NoSuchFunctionException> {
                evaluateFunction("sayHi", param)
            }.printStackTrace()
        }
    }

    fun instance(scope: CoroutineScope) = CclJsonFunctions(
        mapper = SerializationModule.jacksonBaseMapper,
        appScope = scope,
        configurationRepository = cclConfigurationRepository
    )
}
