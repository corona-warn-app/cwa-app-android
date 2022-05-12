package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import de.rki.coronawarnapp.covidcertificate.valueset.internal.toValueSetsContainer
import de.rki.coronawarnapp.server.protocols.internal.dgc.ValueSetsOuterClass
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.util.serialization.fromJson
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.io.FileReader
import java.nio.file.Paths
import java.util.Locale
import java.util.stream.Stream

class CertLogicTestProvider : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        // Load and parse test data
        val jsonFile = Paths.get("src", "test", "resources", "dcc-validation-rules-common-test-cases.json").toFile()
        jsonFile shouldNotBe null
        val jsonString = FileReader(jsonFile).readText()
        jsonString.length shouldBeGreaterThan 0
        val json = SerializationModule.baseGson.fromJson<CertLogicTestCases>(jsonString)
        json shouldNotBe null

        val valueSets = ValueSetsOuterClass.ValueSets.parseFrom(
            json.general.valueSetProtocolBuffer.decodeBase64()!!.toByteArray()
        )
        val container = valueSets.toValueSetsContainer(languageCode = Locale.GERMAN)
        return json.testCases.map { Arguments.of(Named.of(it.description, it), container) }.stream()
    }
}
