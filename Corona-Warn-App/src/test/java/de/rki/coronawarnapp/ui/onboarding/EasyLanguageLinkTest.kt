package de.rki.coronawarnapp.ui.onboarding

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.Locale

class EasyLanguageLinkTest : BaseTest() {

    @BeforeEach
    fun setup() {
        mockkObject(Locale.getDefault())
    }

    @Test
    fun showLinkForGerman() {
        every { Locale.getDefault().language } returns "de"
        showEasyLanguageLink() shouldBe true
    }

    @Test
    fun showNoLinkForEnglish() {
        every { Locale.getDefault().language } returns "en"
        showEasyLanguageLink() shouldBe false
    }

    @Test
    fun showNoLinkForTurkish() {
        every { Locale.getDefault().language } returns "tr"
        showEasyLanguageLink() shouldBe false
    }

    @Test
    fun showNoLinkForBulgarian() {
        every { Locale.getDefault().language } returns "bg"
        showEasyLanguageLink() shouldBe false
    }

    @Test
    fun showNoLinkForPolish() {
        every { Locale.getDefault().language } returns "pl"
        showEasyLanguageLink() shouldBe false
    }

    @Test
    fun showNoLinkForRomanian() {
        every { Locale.getDefault().language } returns "ro"
        showEasyLanguageLink() shouldBe false
    }
}
