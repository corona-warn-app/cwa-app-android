package de.rki.coronawarnapp.bugreporting

import android.content.Context
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.LogUpload
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.UploadHistory
import de.rki.coronawarnapp.bugreporting.settings.BugReportingSettings
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.shouldMatchJson
import testhelpers.preferences.MockSharedPreferences

class BugReportSettingsTest : BaseTest() {
    @MockK lateinit var context: Context
    lateinit var preferences: MockSharedPreferences

    private val baseGson = SerializationModule().baseGson()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        preferences = MockSharedPreferences()
        every { context.getSharedPreferences("bugreporting_localdata", Context.MODE_PRIVATE) } returns preferences
    }

    fun createInstance() = BugReportingSettings(
        context = context,
        gson = baseGson
    )

    @Test
    fun `upload history is empty by default`() {
        val instance = createInstance()
        preferences.dataMapPeek.isEmpty() shouldBe true
        instance.uploadHistory.value shouldBe UploadHistory()
    }

    @Test
    fun `upload history save and load`() {
        var expectedData: UploadHistory? = null
        val instance = createInstance()
        instance.uploadHistory.update {
            it.copy(
                logs = listOf(
                    LogUpload(id = "id1", uploadedAt = Instant.parse("2021-02-01T15:00:00.000Z")),
                    LogUpload(id = "id2", uploadedAt = Instant.parse("2021-02-02T15:00:00.000Z"))
                )
            ).also {
                expectedData = it
            }
        }

        preferences.dataMapPeek["upload.history"] as String shouldMatchJson """
            {
                "logs": [
                    {
                        "id": "id1",
                        "uploadedAt": 1612191600000
                    },
                    {
                        "id": "id2",
                        "uploadedAt": 1612278000000
                    }
                ]
            }
        """

        instance.uploadHistory.value shouldBe expectedData
    }
}
