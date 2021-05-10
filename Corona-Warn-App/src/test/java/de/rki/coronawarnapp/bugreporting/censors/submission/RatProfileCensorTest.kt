package de.rki.coronawarnapp.bugreporting.censors.submission

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfile
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class RatProfileCensorTest : BaseTest() {

    @MockK lateinit var ratProfileSettings: RATProfileSettings

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createInstance() = RatProfileCensor(
        ratProfileSettings = ratProfileSettings
    )

    @Test
    fun `checkLog() should return null if no RAT profile is stored`() = runBlocking {
        every { ratProfileSettings.profile.flow } returns flowOf(null)

        val censor = createInstance()

        val logLine = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Lorem ipsum",
            tag = "I'm a tag",
            throwable = null
        )

        censor.checkLog(logLine) shouldBe null
    }

    @Test
    fun `checkLog() should return null if LogLine doesn't need to be censored`() = runBlocking {
        every { ratProfileSettings.profile.flow } returns flowOf(profile)

        val censor = createInstance()

        val logLine = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Lorem ipsum",
            tag = "I'm a tag",
            throwable = null
        )

        censor.checkLog(logLine) shouldBe null
    }

    @Test
    fun `checkLog() should return censored LogLine`() = runBlocking {
        every { ratProfileSettings.profile.flow } returns flowOf(profile)

        val censor = createInstance()

        val logLine = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Mister First name who is also known as Last name and is born on 1950-08-01 lives in Main street, " +
                "12132 in the beautiful city of London. You can reach him by phone: 111111111 or email: email@example.com",
            tag = "I'm a tag",
            throwable = null
        )

        censor.checkLog(logLine) shouldBe logLine.copy(
            message = "Mister RAT-Profile/FirstName who is also known as RAT-Profile/LastName and is born on RAT-Profile/DateOfBirth lives in RAT-Profile/Street, " +
                "RAT-Profile/Zip-Code in the beautiful city of RAT-Profile/City. You can reach him by phone: RAT-Profile/Phone or email: RAT-Profile/eMail"
        )

        // censoring should still work after the user deletes his profile
        every { ratProfileSettings.profile.flow } returns flowOf(null)

        censor.checkLog(logLine) shouldBe logLine.copy(
            message = "Mister RAT-Profile/FirstName who is also known as RAT-Profile/LastName and is born on RAT-Profile/DateOfBirth lives in RAT-Profile/Street, " +
                "RAT-Profile/Zip-Code in the beautiful city of RAT-Profile/City. You can reach him by phone: RAT-Profile/Phone or email: RAT-Profile/eMail"
        )
    }

    private val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    private val profile = RATProfile(
        firstName = "First name",
        lastName = "Last name",
        birthDate = formatter.parseLocalDate("1950-08-01"),
        street = "Main street",
        zipCode = "12132",
        city = "London",
        phone = "111111111",
        email = "email@example.com"
    )
}
