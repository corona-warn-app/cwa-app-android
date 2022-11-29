package de.rki.coronawarnapp.submission.data

import com.google.gson.Gson
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.SubmissionSettings.Companion.SUBMISSION_SYMPTOMS_LATEST
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.coroutines.runTest2
import testhelpers.preferences.FakeDataStore

class SubmissionSettingsTest {
    private lateinit var dataStore: FakeDataStore
    private lateinit var baseGson: Gson

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        dataStore = FakeDataStore()

        baseGson = SerializationModule().baseGson().newBuilder().apply {
            setPrettyPrinting()
        }.create()
    }

    fun createInstance() = SubmissionSettings(
        dataStore = dataStore,
        baseGson = baseGson
    )

    @Test
    fun `persist symptoms`() = runTest2 {
        createInstance().apply {
            symptoms.first() shouldBe null
            dataStore.data.map {
                it.asMap().keys.isEmpty() shouldBe true
            }

            Symptoms(startOfSymptoms = Symptoms.StartOf.NoInformation, Symptoms.Indication.POSITIVE).let { value ->
                updateSymptoms(value)
                symptoms.first() shouldBe value
                dataStore[SUBMISSION_SYMPTOMS_LATEST] shouldBe """
                    {
                      "startOfSymptoms": {
                        "type": "NoInformation"
                      },
                      "symptomIndication": "POSITIVE"
                    }
                """.trimIndent()
            }

            Symptoms(startOfSymptoms = Symptoms.StartOf.OneToTwoWeeksAgo, Symptoms.Indication.NEGATIVE).let { value ->
                updateSymptoms(value)
                symptoms.first() shouldBe value
                dataStore[SUBMISSION_SYMPTOMS_LATEST] shouldBe """
                    {
                      "startOfSymptoms": {
                        "type": "OneToTwoWeeksAgo"
                      },
                      "symptomIndication": "NEGATIVE"
                    }
                """.trimIndent()
            }

            Symptoms(
                startOfSymptoms = Symptoms.StartOf.MoreThanTwoWeeks,
                Symptoms.Indication.NO_INFORMATION
            ).let { value ->
                updateSymptoms(value)
                symptoms.first() shouldBe value
                dataStore[SUBMISSION_SYMPTOMS_LATEST] shouldBe """
                    {
                      "startOfSymptoms": {
                        "type": "MoreThanTwoWeeks"
                      },
                      "symptomIndication": "NO_INFORMATION"
                    }
                """.trimIndent()
            }

            Symptoms(
                startOfSymptoms = Symptoms.StartOf.LastSevenDays,
                Symptoms.Indication.NO_INFORMATION
            ).let { value ->
                updateSymptoms(value)
                symptoms.first() shouldBe value
                dataStore[SUBMISSION_SYMPTOMS_LATEST] shouldBe """
                    {
                      "startOfSymptoms": {
                        "type": "LastSevenDays"
                      },
                      "symptomIndication": "NO_INFORMATION"
                    }
                """.trimIndent()
            }

            Symptoms(
                startOfSymptoms = Symptoms.StartOf.Date(LocalDate.parse("2020-12-24")),
                Symptoms.Indication.NO_INFORMATION
            ).let { value ->
                updateSymptoms(value)
                symptoms.first() shouldBe value
                dataStore[SUBMISSION_SYMPTOMS_LATEST] shouldBe """
                    {
                      "startOfSymptoms": {
                        "type": "Date",
                        "date": "2020-12-24"
                      },
                      "symptomIndication": "NO_INFORMATION"
                    }
                """.trimIndent()
            }
        }
    }

    @Test
    fun `symptoms default to null`() = runTest2 {
        createInstance().apply {
            symptoms.first() shouldBe null
        }
    }

    @Test
    fun `setting symptoms to null works`() = runTest2 {
        createInstance().apply {
            updateSymptoms(null)
            symptoms.first() shouldBe null
        }
    }
}
