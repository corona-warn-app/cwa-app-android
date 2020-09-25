package de.rki.coronawarnapp.submission

import de.rki.coronawarnapp.BuildConfig
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest

class SubmissionModuleTest : BaseIOTest() {

    private val module = SubmissionModule()

    @Test
    fun `download URL comes from BuildConfig`() {
        module.provideSubmissionServerUrl() shouldBe BuildConfig.SUBMISSION_CDN_URL
    }
}
