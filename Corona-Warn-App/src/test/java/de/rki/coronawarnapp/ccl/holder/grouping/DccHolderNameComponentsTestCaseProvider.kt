package de.rki.coronawarnapp.ccl.holder.grouping

import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.util.serialization.SerializationModule
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.io.FileReader
import java.nio.file.Paths
import java.util.stream.Stream

class DccHolderNameComponentsTestCaseProvider : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val mapper = SerializationModule().jacksonObjectMapper()
        val jsonFile = Paths.get("src", "test", "resources", FILE_NAME).toFile()
        val jsonString = FileReader(jsonFile).readText()
        val testCases = mapper.readValue<TestCasesName>(jsonString)
        return testCases.testCases.map { Arguments.of(Named.of(it.description, it)) }.stream()
    }

    companion object {
        private const val FILE_NAME = "dcc-holder-name-components.gen.json"
    }
}
