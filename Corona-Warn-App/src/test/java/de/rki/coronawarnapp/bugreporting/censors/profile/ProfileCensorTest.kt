package de.rki.coronawarnapp.bugreporting.censors.profile

import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.profile.storage.ProfileRepository
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("MaxLineLength")
internal class ProfileCensorTest : BaseTest() {

    @MockK lateinit var profileRepository: ProfileRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createInstance(scope: CoroutineScope) = ProfileCensor(
        debugScope = scope,
        profileRepository = profileRepository,
    )

    @Test
    fun `checkLog() should return null if no profile is stored`() = runTest(UnconfinedTestDispatcher()) {
        every { profileRepository.profilesFlow } returns flowOf(setOf())
        val logLine = "Lorem ipsum"
        createInstance(this).checkLog(logLine) shouldBe null
    }

    @Test
    fun `checkLog() should return null if LogLine doesn't need to be censored`() = runTest(UnconfinedTestDispatcher()) {
        every { profileRepository.profilesFlow } returns flowOf(setOf(profile))
        val logLine = "I'm a tag"
        createInstance(this).checkLog(logLine) shouldBe null
    }

    @Test
    fun `checkLog() should return censored LogLine`() = runTest(UnconfinedTestDispatcher()) {
        every { profileRepository.profilesFlow } returns flowOf(setOf(profile))

        val logLine =
            "Mister First name who is also known as Last name and is born on 1950-08-01 lives in Main street, " +
                "12132 in the beautiful city of London. You can reach him by phone: 111111111 or email: email@example.com"

        val censored = createInstance(this).checkLog(logLine)!!.compile()!!.censored

        censored.contains(profile.firstName) shouldBe false
        censored.contains(profile.lastName) shouldBe false
        censored.contains(profile.city) shouldBe false
        censored.contains(profile.street) shouldBe false
        censored.contains(profile.zipCode) shouldBe false
        censored.contains(profile.email) shouldBe false
        censored.contains(profile.phone) shouldBe false
        censored.contains(dateOfBirth) shouldBe false
    }

    @Test
    fun `censoring should still work after the user deletes his profile`() = runTest(UnconfinedTestDispatcher()) {
        every { profileRepository.profilesFlow } returns flowOf(setOf(profile), setOf())

        val logLine =
            "Mister First name who is also known as Last name and is born on 1950-08-01 lives in Main street, " +
                "12132 in the beautiful city of London. You can reach him by phone: 111111111 or email: email@example.com"

        val censored = createInstance(this).checkLog(logLine)!!.compile()!!.censored
        censored.contains(profile.firstName) shouldBe false
        censored.contains(profile.lastName) shouldBe false
        censored.contains(profile.city) shouldBe false
        censored.contains(profile.street) shouldBe false
        censored.contains(profile.zipCode) shouldBe false
        censored.contains(profile.email) shouldBe false
        censored.contains(profile.phone) shouldBe false
        censored.contains(dateOfBirth) shouldBe false
    }

    @Test
    fun `self overlap`() = runTest(UnconfinedTestDispatcher()) {
        val selfOverlap = profile.copy(
            lastName = "Berlin",
            city = "Berlin Kreuzberg"
        )
        every { profileRepository.profilesFlow } returns flowOf(setOf(selfOverlap), setOf())

        val logLine =
            "Mister First name who is also known as Last name and is born on 1950-08-01 lives in Main street, " +
                "12132 in the beautiful city of Berlin Kreuzberg. You can reach him by phone: 111111111 or email: email@example.com"

        val censored = createInstance(this).checkLog(logLine)!!.compile()!!.censored
        censored.contains(selfOverlap.firstName) shouldBe false
        censored.contains(selfOverlap.lastName) shouldBe false
        censored.contains(selfOverlap.city) shouldBe false
        censored.contains(selfOverlap.street) shouldBe false
        censored.contains(selfOverlap.zipCode) shouldBe false
        censored.contains(selfOverlap.email) shouldBe false
        censored.contains(selfOverlap.phone) shouldBe false
        censored.contains(dateOfBirth) shouldBe false
    }

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val dateOfBirth = "1950-08-01"

    private val profile = Profile(
        firstName = "First name",
        lastName = "Last name",
        birthDate = LocalDate.parse(dateOfBirth, formatter),
        street = "Main street",
        zipCode = "12132",
        city = "London",
        phone = "111111111",
        email = "email@example.com"
    )
}
