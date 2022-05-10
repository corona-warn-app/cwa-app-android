package de.rki.coronawarnapp.covidcertificate.validation.core.country

import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationCache
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class DccCountryLocalCacheTest : BaseIOTest() {

    private val cacheDir = File(IO_TEST_BASEDIR, "cache")

    private fun createInstance(): DccValidationCache = DccValidationCache(
        cacheDir = cacheDir
    )

    @Test
    fun `write and read data`() = runTest {

        createInstance().apply {
            loadCountryJson() shouldBe null
            saveCountryJson("{äöü")
        }

        createInstance().apply {
            loadCountryJson() shouldBe "{äöü"
            saveCountryJson(null)
            loadCountryJson() shouldBe null
        }
    }
}
