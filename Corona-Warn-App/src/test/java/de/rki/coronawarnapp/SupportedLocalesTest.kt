package de.rki.coronawarnapp

import io.kotest.matchers.shouldBe
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
            "uk"
        ).sorted()
        BuildConfig.SUPPORTED_LOCALES.toList().sorted() shouldBe expectedSupportedLocales

        val values = File("./src/main/res")
            .listFiles()!!
            .map { it.name.substringAfterLast("/") }
            .filter { it == "values" || it.matches("values-[a-z]{2}$".toRegex()) }
            .map { if (it == "values") "en" else it.substringAfterLast("-") }
            .sorted()

        values shouldBe expectedSupportedLocales

        Timber.d("Locales in values=$values")
    }
}
