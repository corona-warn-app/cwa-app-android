package de.rki.coronawarnapp.risk

import io.kotest.matchers.shouldBe
import org.joda.time.Duration
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RiskLevelTaskConfigTest : BaseTest() {

    @Test
    fun `risk level task max execution time is not above 9 minutes`() {
        val config = RiskLevelTask.Config()
        config.executionTimeout.isShorterThan(Duration.standardMinutes(9)) shouldBe true
    }
}
