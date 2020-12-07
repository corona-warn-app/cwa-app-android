package de.rki.coronawarnapp.submission.data

import android.content.Context
import com.google.gson.Gson
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.preferences.MockSharedPreferences

class SubmissionSettingsTest {
    @MockK lateinit var context: Context
    private lateinit var mockPreferences: MockSharedPreferences
    private lateinit var baseGson: Gson

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockPreferences = MockSharedPreferences()

        every {
            context.getSharedPreferences("submission_localdata", Context.MODE_PRIVATE)
        } returns mockPreferences

        baseGson = SerializationModule().baseGson().newBuilder().apply {
            setPrettyPrinting()
        }.create()
    }

    fun createInstance() = SubmissionSettings(
        context = context,
        baseGson = baseGson
    )

    @Test
    fun consentIsPersisted() {
        createInstance().apply {
            hasGivenConsent.value shouldBe false
            hasGivenConsent.update { true }
            hasGivenConsent.value shouldBe true
        }
    }

    @Test
    fun `persist symptoms`() {
        createInstance().apply {
            symptoms.value shouldBe null
            mockPreferences.dataMapPeek.isEmpty() shouldBe true

            Symptoms(startOfSymptoms = Symptoms.StartOf.NoInformation, Symptoms.Indication.POSITIVE).let { value ->
                symptoms.update { value }
                symptoms.value shouldBe value
                mockPreferences.dataMapPeek["submission.symptoms.latest"] shouldBe """
                    {
                      "startOfSymptoms": {
                        "type": "NoInformation"
                      },
                      "symptomIndication": "POSITIVE"
                    }
                """.trimIndent()
            }

            Symptoms(startOfSymptoms = Symptoms.StartOf.OneToTwoWeeksAgo, Symptoms.Indication.NEGATIVE).let { value ->
                symptoms.update { value }
                symptoms.value shouldBe value
                mockPreferences.dataMapPeek["submission.symptoms.latest"] shouldBe """
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
                symptoms.update { value }
                symptoms.value shouldBe value
                mockPreferences.dataMapPeek["submission.symptoms.latest"] shouldBe """
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
                symptoms.update { value }
                symptoms.value shouldBe value
                mockPreferences.dataMapPeek["submission.symptoms.latest"] shouldBe """
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
                symptoms.update { value }
                symptoms.value shouldBe value
                mockPreferences.dataMapPeek["submission.symptoms.latest"] shouldBe """
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
    fun `symptoms default to null`() {
        createInstance().apply {
            symptoms.value shouldBe null
        }
    }
}
