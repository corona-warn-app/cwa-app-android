package de.rki.coronawarnapp.service.submission

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class VerificationServiceTest {

    @Test
    fun containsValidGUID() {
        // valid
        val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
        assertThat(
            SubmissionService.containsValidGUID("https://bs-sd.de/covid-19/?$guid"),
            equalTo(true)
        )

        // invalid
        assertThat(
            SubmissionService.containsValidGUID("https://no-guid-here"),
            equalTo(false)
        )
    }

    @Test
    fun extractGUID() {
        val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
        assertThat(
            SubmissionService.extractGUID("https://bs-sd.de/covid-19/?$guid"),
            equalTo(guid)
        )
    }
}
