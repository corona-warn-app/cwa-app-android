package de.rki.coronawarnapp.profile

import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.profile.model.ProfileId
import de.rki.coronawarnapp.profile.storage.ProfileRepository
import de.rki.coronawarnapp.profile.ui.create.CreateProfileNavigation
import de.rki.coronawarnapp.profile.ui.create.ProfileCreateFragmentViewModel
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import java.time.format.DateTimeFormatter

@ExtendWith(InstantExecutorExtension::class)
internal class ProfileDataCreateFragmentViewModelTest : BaseTest() {

    @MockK lateinit var profileRepository: ProfileRepository

    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val birthDate: LocalDate = LocalDate.parse("01.01.1980", formatter)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { profileRepository.profilesFlow } returns flowOf(emptySet())
        coEvery { profileRepository.upsertProfile(any()) } returns 1
    }

    @Test
    fun `createProfile doesn't create profile when profile is invalid`() {
        viewModel().apply {
            saveProfile()
            profile.getOrAwaitValue().isValid shouldBe false
        }

        verify(exactly = 1) {
            profileRepository.profilesFlow
        }
    }

    @Test
    fun `Saved profile is displayed`() {
        val savedProfile = Profile(
            id = 1,
            firstName = "First name",
            lastName = "Last name",
            birthDate = birthDate,
            street = "Main street",
            zipCode = "12132",
            city = "London",
            phone = "111111111",
            email = "email@example.com"
        )
        every { profileRepository.profilesFlow } returns flowOf(setOf(savedProfile))

        viewModel(savedProfile.id).apply {
            // Fields updated
            firstNameChanged(savedProfile.firstName)
            lastNameChanged(savedProfile.lastName)
            birthDateChanged(savedProfile.birthDate?.format(formatter))
            streetChanged(savedProfile.street)
            zipCodeChanged(savedProfile.zipCode)
            cityChanged(savedProfile.city)
            phoneChanged(savedProfile.phone)
            emailChanged(savedProfile.email)

            val input = this.savedProfile.getOrAwaitValue()!!
            val output = profile.getOrAwaitValue()
            input.firstName shouldBe output.firstName
            input.lastName shouldBe output.lastName
            input.phone shouldBe output.phone
            input.email shouldBe output.email
            input.city shouldBe output.city
            input.street shouldBe output.street
            input.zipCode shouldBe output.zipCode
        }
    }

    @Test
    fun `createProfile create profile when at least one field is set`() {
        viewModel().apply {
            firstNameChanged("First name")
            saveProfile()
            events.getOrAwaitValue() shouldBe CreateProfileNavigation.ProfileScreen(1)
        }

        verify {
            profileRepository.profilesFlow
        }
    }

    @Test
    fun firstNameChanged() {
        viewModel().apply {
            firstNameChanged("First name")
            profile.getOrAwaitValue().apply {
                firstName shouldBe "First name"
                isValid shouldBe true
            }
        }
    }

    @Test
    fun lastNameChanged() {
        viewModel().apply {
            lastNameChanged("Last name")
            profile.getOrAwaitValue().apply {
                lastName shouldBe "Last name"
                isValid shouldBe true
            }
        }
    }

    @Test
    fun birthDateChanged() {
        viewModel().apply {
            birthDateChanged("01.01.2021")
            profile.getOrAwaitValue().apply {
                birthDate shouldBe birthDate
                isValid shouldBe true
            }
        }
    }

    @Test
    fun streetChanged() {
        viewModel().apply {
            streetChanged("Main St.")
            profile.getOrAwaitValue().apply {
                street shouldBe "Main St."
                isValid shouldBe true
            }
        }
    }

    @Test
    fun zipCodeChanged() {
        viewModel().apply {
            zipCodeChanged("11111")
            profile.getOrAwaitValue().apply {
                zipCode shouldBe "11111"
                isValid shouldBe true
            }
        }
    }

    @Test
    fun cityChanged() {
        viewModel().apply {
            cityChanged("London")
            profile.getOrAwaitValue().apply {
                city shouldBe "London"
                isValid shouldBe true
            }
        }
    }

    @Test
    fun phoneChanged() {
        viewModel().apply {
            phoneChanged("111111111")
            profile.getOrAwaitValue().apply {
                phone shouldBe "111111111"
                isValid shouldBe true
            }
        }
    }

    @Test
    fun emailChanged() {
        viewModel().apply {
            emailChanged("email@example.com")
            profile.getOrAwaitValue().apply {
                email shouldBe "email@example.com"
                isValid shouldBe true
            }
        }
    }

    @Test
    fun allFieldsAreSet() {
        viewModel().apply {
            firstNameChanged("First name")
            lastNameChanged("Last name")
            birthDateChanged("01.01.1980")
            streetChanged("Main street")
            zipCodeChanged("12132")
            cityChanged("London")
            phoneChanged("111111111")
            emailChanged("email@example.com")
            profile.getOrAwaitValue().apply {
                isValid shouldBe true
                this shouldBe Profile(
                    firstName = "First name",
                    lastName = "Last name",
                    birthDate = birthDate,
                    street = "Main street",
                    zipCode = "12132",
                    city = "London",
                    phone = "111111111",
                    email = "email@example.com"
                )
            }
        }
    }

    @Test
    fun navigateBack() {
        viewModel().apply {
            navigateBack()
            events.getOrAwaitValue() shouldBe CreateProfileNavigation.Back
        }
    }

    fun viewModel(profileId: ProfileId? = null) =
        ProfileCreateFragmentViewModel(profileRepository, formatter, profileId)
}
