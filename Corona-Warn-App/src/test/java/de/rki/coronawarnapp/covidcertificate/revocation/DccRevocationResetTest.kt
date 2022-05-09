package de.rki.coronawarnapp.covidcertificate.revocation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import okhttp3.Cache
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2
import java.io.IOException

class DccRevocationResetTest : BaseTest() {

    @RelaxedMockK lateinit var cache: Cache
    @RelaxedMockK lateinit var dataStore: DataStore<Preferences>

    private val instance: DccRevocationReset
        get() = DccRevocationReset(
            cache = cache,
            dataStore = dataStore
        )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    @Suppress("BlockingMethodInNonBlockingContext")
    fun `clears all data`() = runBlockingTest2 {
        instance.reset()

        coVerify {
            cache.evictAll()
            dataStore.updateData(any())
        }
    }

    @Test
    fun `clear does not throw`() = runBlockingTest2 {
        coEvery { dataStore.updateData(any()) } throws IOException("Test error")

        shouldNotThrowAny {
            instance.reset()
        }
    }
}
