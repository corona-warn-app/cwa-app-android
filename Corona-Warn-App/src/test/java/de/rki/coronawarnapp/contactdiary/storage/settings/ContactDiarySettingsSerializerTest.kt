package de.rki.coronawarnapp.contactdiary.storage.settings

import androidx.datastore.core.CorruptionException
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ContactDiarySettingsSerializerTest : BaseTest() {

    private val objectMapper = SerializationModule.jacksonBaseMapper
    private val serializer = ContactDiarySettingsSerializer(objectMapper)

    @Test
    fun `check defaultValue`() {
        serializer.defaultValue shouldBe ContactDiarySettings()
    }

    @Test
    fun `read value`() = runTest {
    }

    @Test
    fun `write value`() = runTest {
    }

    @Test
    fun `throws CorruptionException for invalid data`() = runTest {
        shouldThrow<CorruptionException> { "Invalid Data".byteInputStream().use { serializer.readFrom(it) } }
    }
}
