package de.rki.coronawarnapp.bugreporting

import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class BugReporterTest : BaseTest() {

    @Test
    fun `test emtpy tag`() {
        // This just tests the timber statement
        Exception().reportProblem(info = "info")
    }

    @Test
    fun `test empty info and tag`() {
        // This just tests the timber statement
        Exception().reportProblem()
    }
}
