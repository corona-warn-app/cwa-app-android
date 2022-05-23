package de.rki.coronawarnapp.bugreporting.debuglog.upload.history.storage

import androidx.datastore.core.CorruptionException
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.LogUpload
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.UploadHistory
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.shouldMatchJson
import java.io.ByteArrayOutputStream
import java.time.Instant

class UploadHistorySerializerTest : BaseTest() {

    private val objectMapper = SerializationModule.jacksonBaseMapper
    private val serializer = UploadHistorySerializer(objectMapper)

    private val testUploadHistory = UploadHistory(
        logs = listOf(
            LogUpload(id = "id1", uploadedAt = Instant.parse("2021-02-01T15:00:00.000Z")),
            LogUpload(id = "id2", uploadedAt = Instant.parse("2021-02-02T15:00:00.000Z"))
        )
    )

    private val testUploadHistoryJson = """
        {
          "logs": [
            {
              "id": "id1",
              "uploadedAt": 1612191600.000000000
            },
            {
              "id": "id2",
              "uploadedAt": 1612278000.000000000
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `defaultValue is empty UploadHistory`() {
        serializer.defaultValue shouldBe UploadHistory()
    }

    @Test
    fun `read UploadHistory`() = runTest {
        testUploadHistoryJson
            .byteInputStream()
            .use { serializer.readFrom(it) } shouldBe testUploadHistory
    }

    @Test
    fun `write UploadHistory`() = runTest {
        ByteArrayOutputStream()
            .apply { use { serializer.writeTo(testUploadHistory, it) } }
            .toString() shouldMatchJson testUploadHistoryJson
    }

    @Test
    fun `throws CorruptionException for invalid data`() = runTest {
        shouldThrow<CorruptionException> { "Invalid Data".byteInputStream().use { serializer.readFrom(it) } }
    }
}
