package de.rki.coronawarnapp.ccl.dccadmission.model

import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.ccl.dccadmission.admissionCheckScenarios
import de.rki.coronawarnapp.ccl.dccadmission.scenariosJson
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SystemTime
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty

class DccAdmissionCheckScenariosTest : BaseTest() {

    private val mapper = SerializationModule().jacksonObjectMapper()

    private val input = DccAdmissionCheckScenariosInput(
        os = "android",
        language = "en",
        now = SystemTime(
            timestamp = 1640854800,
            localDate = "2021-12-30",
            localDateTime = "2021-12-30T10:00:00+01:00",
            localDateTimeMidnight = "2021-12-30T00:00:00+01:00",
            utcDate = "2021-12-30",
            utcDateTime = "2021-12-30T09:00:00Z",
            utcDateTimeMidnight = "2021-12-30T00:00:00Z"
        )
    )

    private val inputJson = """
        {
          "os": "android",
          "language": "en",
          "now": {
            "timestamp": 1640854800,
            "localDate": "2021-12-30",
            "localDateTime": "2021-12-30T10:00:00+01:00",
            "localDateTimeMidnight": "2021-12-30T00:00:00+01:00",
            "utcDate": "2021-12-30",
            "utcDateTime": "2021-12-30T09:00:00Z",
            "utcDateTimeMidnight": "2021-12-30T00:00:00Z"
          }
        }
    """.trimIndent()

    @Test
    fun `parse output`() {
        mapper.readValue<DccAdmissionCheckScenarios>(scenariosJson) shouldBe admissionCheckScenarios
        mapper.writeValueAsString(admissionCheckScenarios).toComparableJsonPretty() shouldBe scenariosJson
    }

    @Test
    fun `parse input`() {
        mapper.readValue<DccAdmissionCheckScenariosInput>(inputJson) shouldBe input
        mapper.writeValueAsString(input).toComparableJsonPretty() shouldBe inputJson
    }
}
