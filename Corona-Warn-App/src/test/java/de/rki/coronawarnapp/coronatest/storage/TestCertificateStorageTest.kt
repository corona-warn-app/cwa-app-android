package de.rki.coronawarnapp.coronatest.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences

class TestCertificateStorageTest : BaseTest() {
    @MockK lateinit var context: Context
    private lateinit var mockPreferences: MockSharedPreferences

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockPreferences = MockSharedPreferences()

        every {
            context.getSharedPreferences("coronatest_certificate_localdata", Context.MODE_PRIVATE)
        } returns mockPreferences
    }

    private fun createInstance() = TestCertificateStorage(
        context = context,
        baseGson = SerializationModule().baseGson()
    )

    @Test
    fun `init is sideeffect free`() {
        createInstance()
    }

    @Test
    fun `storing empty set deletes data`() {
        mockPreferences.edit {
            putString("dontdeleteme", "test")
            putString("testcertificate.data.ra", "test")
            putString("testcertificate.data.pcr", "test")
        }
        createInstance().testCertificates = emptySet()

        mockPreferences.dataMapPeek.keys.single() shouldBe "dontdeleteme"
    }
}
