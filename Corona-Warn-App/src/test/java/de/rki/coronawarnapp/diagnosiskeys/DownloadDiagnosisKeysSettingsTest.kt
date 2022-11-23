package de.rki.coronawarnapp.diagnosiskeys

import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysSettings
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysSettings.Companion.KEY_LAST_VERSION_CODE
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.preferences.FakeDataStore
import java.time.Instant

class DownloadDiagnosisKeysSettingsTest : BaseTest() {
    lateinit var dataStore: FakeDataStore
    private val gson = SerializationModule().baseGson()

    private val lastDownload = DownloadDiagnosisKeysSettings.LastDownload(
        startedAt = Instant.EPOCH,
        finishedAt = Instant.EPOCH,
        successful = false,
        newData = false
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        dataStore = FakeDataStore()

        mockkObject(BuildConfigWrap)
    }

    fun createInstance() = DownloadDiagnosisKeysSettings(
        gson = gson,
        dataStore = dataStore
    )

    @Test
    fun `LastDownload is correctly built`() = runTest2 {
        val instance = createInstance()

        with(instance) {
            lastDownloadDays.first() shouldBe null
            updateLastDownloadDays(lastDownload)
            lastDownloadDays.first() shouldBe lastDownload

            lastDownloadHours.first() shouldBe null
            updateLastDownloadHours(lastDownload)
            lastDownloadHours.first() shouldBe lastDownload
        }
    }

    @Test
    fun `lastVersionCode default values`() = runTest2 {
        val instance = createInstance()
        instance.lastVersionCode.first() shouldBe -1L
        instance.isUpdateToEnfV2() shouldBe true
    }

    @Test
    fun `lastVersionCode on 1_8_x`() = runTest2 {
        dataStore[KEY_LAST_VERSION_CODE] = 1080000
        val instance = createInstance()
        instance.lastVersionCode.first() shouldBe 1080000L
        instance.isUpdateToEnfV2() shouldBe false
    }

    @Test
    fun `update the last versionCode`() = runTest2 {
        val instance = createInstance()
        instance.updateLastVersionCode(99)
        instance.isUpdateToEnfV2() shouldBe true
        instance.lastVersionCode.first() shouldBe 99
        instance.isUpdateToEnfV2() shouldBe true

        every { BuildConfigWrap.VERSION_CODE } returns 1080000

        instance.updateLastVersionCodeToCurrent()
        instance.lastVersionCode.first() shouldBe 1080000
        instance.isUpdateToEnfV2() shouldBe false
        dataStore[KEY_LAST_VERSION_CODE] shouldBe 1080000
    }
}
