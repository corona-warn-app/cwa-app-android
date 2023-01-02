package de.rki.coronawarnapp

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.File

class TranslationsAssertTest : BaseTest() {

    @Test
    fun assertTranslationsFiles() {
        val translationsFiles = File("./src/main/res/values-de")
            .listFiles()
            ?.filter { it.name.contains("strings.xml") && !it.name.contains("legal") }
            ?.map { it.name.substringAfterLast("/") }
            .orEmpty()
            .sortedBy { it }
            .toList()

        val json = File(
            File("./").absoluteFile.toString().substringBeforeLast("/Corona-Warn-App"),
            "translation_v2.json"
        ).readBytes().toString(Charsets.UTF_8)

        val fileNames = ObjectMapper().readTree(json)["collections"]
            .get(0)["folders"].get(0)["sourceFilters"]
            .map { it.asText() }
            .sortedBy { it.toString() }
            .toList()

        translationsFiles shouldBe fileNames
    }
}
