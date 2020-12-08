package de.rki.coronawarnapp.diagnosiskeys

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysSettings
import de.rki.coronawarnapp.diagnosiskeys.download.isUpdateToEnfV2
import de.rki.coronawarnapp.diagnosiskeys.download.updateLastVersionCodeToCurrent
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences

class DownloadDiagnosisKeysSettingsTest : BaseTest() {
    @MockK lateinit var context: Context
    lateinit var preferences: MockSharedPreferences

    private val baseGson = SerializationModule().baseGson()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        preferences = MockSharedPreferences()
        every { context.getSharedPreferences("keysync_localdata", Context.MODE_PRIVATE) } returns preferences

        mockkObject(BuildConfigWrap)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    fun createInstance() = DownloadDiagnosisKeysSettings(
        context = context,
        gson = baseGson
    )

    @Test
    fun `lastVersionCode default values`() {
        val instance = createInstance()
        instance.lastVersionCode shouldBe -1L
        instance.isUpdateToEnfV2 shouldBe true
    }

    @Test
    fun `lastVersionCode on 1_8_x`() {
        preferences.edit {
            putLong("download.task.last.versionCode", 1080000)
        }
        val instance = createInstance()
        instance.lastVersionCode shouldBe 1080000L
        instance.isUpdateToEnfV2 shouldBe false
    }

    @Test
    fun `update the last versionCode`() {
        val instance = createInstance()
        instance.lastVersionCode = 99
        instance.isUpdateToEnfV2 shouldBe true
        instance.lastVersionCode shouldBe 99
        instance.isUpdateToEnfV2 shouldBe true

        every { BuildConfigWrap.VERSION_CODE } returns 1080000

        instance.updateLastVersionCodeToCurrent()
        instance.lastVersionCode shouldBe 1080000
        instance.isUpdateToEnfV2 shouldBe false
        preferences.dataMapPeek["download.task.last.versionCode"] shouldBe 1080000
    }
}
