package de.rki.coronawarnapp.service.submission

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class SubmissionServiceTest {

    @Test
    fun extractGUID() {
        // valid
        val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
        assertThat(
            SubmissionService.extractGUID("https://bs-sd.de/covid-19/?$guid"),
            equalTo(guid)
        )

        // invalid
        assertThat(
            SubmissionService.extractGUID("https://no-guid-here"),
            nullValue()
        )
    }
}
