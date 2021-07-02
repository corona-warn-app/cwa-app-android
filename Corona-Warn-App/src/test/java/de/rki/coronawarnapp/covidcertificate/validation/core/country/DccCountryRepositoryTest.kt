package de.rki.coronawarnapp.covidcertificate.validation.core.country

import de.rki.coronawarnapp.covidcertificate.validation.core.country.server.DccCountryServer
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2

class DccCountryRepositoryTest : BaseTest() {
    @MockK lateinit var server: DccCountryServer
    @MockK lateinit var localCache: DccValidationCache

    private val testCountryData = "[\"DE\",\"NL\"]"
    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        localCache.apply {
            coEvery { loadJson() } returns null
            coEvery { saveJson(any()) } just Runs
        }

        server.apply {
            coEvery { dccCountryJson() } returns testCountryData
        }
    }

    private fun createInstance(scope: CoroutineScope) = DccCountryRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        gson = SerializationModule().baseGson(),
        server = server,
        localCache = localCache,
    )

    @Test
    fun `local cache is loaded on init - no server request`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).apply {
            dccCountries.first() shouldBe emptyList()
        }

        coVerify {
            localCache.loadJson()
        }
        coVerify(exactly = 0) {
            server.dccCountryJson()
        }
    }

    @Test
    fun `refresh talks to server and updates local cache`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).apply {
            refresh()
            dccCountries.first() shouldBe listOf(
                DccCountry("DE"), DccCountry("NL")
            )
        }

        coVerify {
            server.dccCountryJson()
            localCache.saveJson(testCountryData)
        }
    }
}
