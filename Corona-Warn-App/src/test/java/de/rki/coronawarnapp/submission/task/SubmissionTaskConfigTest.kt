package de.rki.coronawarnapp.submission.task

import io.kotest.matchers.comparables.shouldBeLessThan
import org.joda.time.Duration
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class SubmissionTaskConfigTest : BaseTest() {

    @Test
    fun `task timeout below 9 minutes`() {
        SubmissionTask.Config().executionTimeout.shouldBeLessThan(Duration.standardMinutes(9))
    }
}
