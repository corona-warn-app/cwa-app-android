package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import com.fasterxml.jackson.databind.node.TextNode
import de.rki.coronawarnapp.ccl.configuration.model.CclConfiguration
import de.rki.coronawarnapp.ccl.configuration.model.FunctionDefinition
import de.rki.coronawarnapp.ccl.configuration.model.FunctionParameter
import de.rki.coronawarnapp.ccl.configuration.model.JsonFunctionsDescriptor
import de.rki.coronawarnapp.ccl.configuration.storage.CclConfigurationRepository
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.jfn.error.NoSuchFunctionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runTest2

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

    private val cclConfiguration = CclConfiguration(
        identifier = "CCL-DE-0001",
        type = CclConfiguration.Type.CCL_CONFIGURATION,
        country = "DE",
        version = "1.0.0",
        schemaVersion = "1.0.0",
        engine = "JsonFunctions",
        engineVersion = "1.0.0",
        _validFrom = "2021-10-07T00:00:00Z",
        _validTo = "2030-06-01T00:00:00Z",
        logic = CclConfiguration.Logic(jfnDescriptors = listOf(jfnDescriptor))
    )

    @MockK private lateinit var cclConfigurationRepository: CclConfigurationRepository

    private var cclConfigurationFlow = MutableStateFlow(listOf(cclConfiguration))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { cclConfigurationRepository.cclConfigurations } returns cclConfigurationFlow
    }

    @Test
    fun evaluateFunction() = runTest2 {
        instance(this).apply {
            evaluateFunction("greet", param).asText() shouldBe "Hello Android!"
            shouldThrow<NoSuchFunctionException> {
                evaluateFunction("sayHi", param)
            }

            val jfnDescriptor = JsonFunctionsDescriptor(
                name = "sayHi",
                definition = FunctionDefinition(
                    parameters = listOf(param1, param2),
                    logic = listOf(jfnLogic)
                )
            )
            val newConfig =
                cclConfiguration.copy(logic = CclConfiguration.Logic(jfnDescriptors = listOf(jfnDescriptor)))
            cclConfigurationFlow.value = listOf(newConfig)
            evaluateFunction("sayHi", param).asText() shouldBe "Hello Android!"
            shouldThrow<NoSuchFunctionException> {
                evaluateFunction("greet", param)
            }
        }
    }

    fun instance(scope: CoroutineScope) = CclJsonFunctions(
        mapper = SerializationModule.jacksonBaseMapper,
        appScope = scope,
        configurationRepository = cclConfigurationRepository,
        dispatcher = TestDispatcherProvider()
    )
}
