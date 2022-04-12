package de.rki.coronawarnapp.covidcertificate.revocation.calculation

import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.util.serialization.fromJson
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream
import kotlin.io.path.div

class RevocationCalculationTestCaseProvider : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val gson = SerializationModule().baseGson()
        val jsonFile = javaClass.classLoader!!.getResourceAsStream("revocation/RevocationCalculationSampleData.json")
        val testCases: List<RevocationCalculationTestCase> = jsonFile.bufferedReader().use { gson.fromJson(it) }
        return testCases.map { Arguments.of(Named.of(it.description, it)) }.stream()
    }
}
