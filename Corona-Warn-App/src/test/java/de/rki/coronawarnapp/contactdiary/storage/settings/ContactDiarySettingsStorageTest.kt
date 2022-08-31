package de.rki.coronawarnapp.contactdiary.storage.settings

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.FakeTypedDataStore

class ContactDiarySettingsStorageTest : BaseTest() {

    private val defaultContactDiarySettings = ContactDiarySettings()
    private val riskContactDiarySettings = ContactDiarySettings(ContactDiarySettings.OnboardingStatus.RISK_STATUS_1_12)

    @MockK lateinit var serializer: ContactDiarySettingsSerializer

    private val dataStore = FakeTypedDataStore(defaultValue = defaultContactDiarySettings, shouldLog = true)

    private val instance: ContactDiarySettingsStorage
        get() = ContactDiarySettingsStorage(
            dataStore = dataStore,
            serializer = serializer
        )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { serializer.defaultValue } returns defaultContactDiarySettings
    }

    @AfterEach
    fun cleanup() {
        dataStore.reset()
    }

    @Test
    fun `loads from DataStore`() = runTest {
        instance.contactDiarySettings.first() shouldBe defaultContactDiarySettings

        dataStore.updateData { riskContactDiarySettings }

        instance.contactDiarySettings.first() shouldBe riskContactDiarySettings
    }

    @Test
    fun `save and load`() = runTest {
        with(instance) {
            dataStore.data.first() shouldBe defaultContactDiarySettings
            contactDiarySettings.first() shouldBe defaultContactDiarySettings

            updateContactDiarySettings { riskContactDiarySettings }

            dataStore.data.first() shouldBe riskContactDiarySettings
            contactDiarySettings.first() shouldBe riskContactDiarySettings
        }
    }

    @Test
    fun `reset data`() = runTest {
        with(instance) {
            updateContactDiarySettings { riskContactDiarySettings }
            dataStore.data.first() shouldBe riskContactDiarySettings
            contactDiarySettings.first() shouldBe riskContactDiarySettings

            reset()

            dataStore.data.first() shouldBe defaultContactDiarySettings
            contactDiarySettings.first() shouldBe defaultContactDiarySettings
        }
    }
}
