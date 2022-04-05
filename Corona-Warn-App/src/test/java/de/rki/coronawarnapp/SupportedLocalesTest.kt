package de.rki.coronawarnapp

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import java.io.File

class SupportedLocalesTest : BaseTest() {
    @Test
    fun assertSupportedLocales() {
        val expectedSupportedLocales = listOf(
            "de",
            "en",
            "tr",
            "bg",
            "pl",
            "ro",
            "uk",
        ).sorted()
        BuildConfig.SUPPORTED_LOCALES.toList().sorted().apply {
            this shouldBe expectedSupportedLocales
            size shouldBe expectedSupportedLocales.size
        }

        val regex = "values-[a-z]{2}$".toRegex()
        val values = File("./src/main/res")
            .listFiles()!!
            .map { it.name.substringAfterLast("/") }
            .filter { it == "values" || it.matches(regex) }
            .map { if (it == "values") "en" else it.substringAfterLast("-") }
            .sorted()

        val message = """
            New Locale detected in `res` directory!. Make sure to update the following:
            - `supportedLocales` array in build.gradle file
            - `CWADateTimeFormatPatternFactory` 
            - `CWADateTimeFormatPatternFactoryTest`
            - `NewReleaseInfoFragmentTest`
            - run the App and check newly added localization and ccl localization
        """.trimIndent()

        expectedSupportedLocales.size shouldBe values.size
        Assertions.assertIterableEquals(expectedSupportedLocales, values, message)

        Timber.d("Locales in values=$values")
    }
}
