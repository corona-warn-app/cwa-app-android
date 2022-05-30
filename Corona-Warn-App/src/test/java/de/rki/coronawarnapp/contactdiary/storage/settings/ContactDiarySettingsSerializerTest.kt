package de.rki.coronawarnapp.contactdiary.storage.settings

import androidx.datastore.core.CorruptionException
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.shouldMatchJson
import java.io.ByteArrayOutputStream

class ContactDiarySettingsSerializerTest : BaseTest() {

    private val objectMapper = SerializationModule.jacksonBaseMapper
    private val serializer = ContactDiarySettingsSerializer(objectMapper)

    private val testContactDiarySettings = ContactDiarySettings(
        onboardingStatus = ContactDiarySettings.OnboardingStatus.RISK_STATUS_1_12
    )
    private val testContactDiarySettingsJson = """{"onboardingStatus":"RISK_STATUS_1_12"}"""

    @Test
    fun `check defaultValue`() {
        serializer.defaultValue shouldBe ContactDiarySettings()
    }

    @Test
    fun `read value`() = runTest {
        testContactDiarySettingsJson
            .byteInputStream()
            .use { serializer.readFrom(it) } shouldBe testContactDiarySettings
    }

    @Test
    fun `write value`() = runTest {
        ByteArrayOutputStream()
            .apply { use { serializer.writeTo(testContactDiarySettings, it) } }
            .toString() shouldMatchJson testContactDiarySettingsJson
    }

    @Test
    fun `throws CorruptionException for invalid data`() = runTest {
        shouldThrow<CorruptionException> { "Invalid Data".byteInputStream().use { serializer.readFrom(it) } }
    }
}
