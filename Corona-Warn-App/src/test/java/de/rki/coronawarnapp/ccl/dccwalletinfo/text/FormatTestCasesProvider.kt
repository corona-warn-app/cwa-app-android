package de.rki.coronawarnapp.ccl.dccwalletinfo.text

import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.util.serialization.SerializationModule
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.io.File
import java.util.stream.Stream
import org.junit.jupiter.api.Named

class FormatTestCasesProvider : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val testsFile = File("src/test/resources/ccl/ccl-text-descriptor-test-cases.gen.json")
        val testCases = SerializationModule().jacksonObjectMapper().readValue<TestCases>(testsFile)
        return testCases.testCases
             .filter { it.description == "placeholder of type localDate" }
            .map { Arguments.of(Named.of(it.description, it)) }.stream()
    }
}
