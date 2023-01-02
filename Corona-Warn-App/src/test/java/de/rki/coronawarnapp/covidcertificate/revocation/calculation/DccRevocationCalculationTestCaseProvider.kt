package de.rki.coronawarnapp.covidcertificate.revocation.calculation

import de.rki.coronawarnapp.util.serialization.SerializationModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class DccRevocationCalculationTestCaseProvider : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val mapper = SerializationModule().jacksonObjectMapper()
        val jsonFile = javaClass.classLoader!!.getResourceAsStream("revocation/RevocationCalculationSampleData.json")
        val testCaseDccs: List<DccRevocationCalculationTestCase> =
            jsonFile.bufferedReader().use { mapper.readValue(it) }
        return testCaseDccs.map { Arguments.of(Named.of(it.description, it)) }.stream()
    }
}
