package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import testhelpers.preferences.mockFlowPreference

@ExtendWith(InstantExecutorExtension::class)
internal class RATProfileDataCreateFragmentViewModelTest : BaseTest() {

    @MockK lateinit var ratProfileSettings: RATProfileSettings

    private val formatter = DateTimeFormat.forPattern("dd.MM.yyyy")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { ratProfileSettings.profile } returns mockFlowPreference(null)
    }

    @Test
    fun `createProfile nothing is set does not create profile`() {
        viewModel().apply {
            createProfile()
        }

        verify(exactly = 0) {
            ratProfileSettings.profile
        }
    }

    @Test
    fun `createProfile create profile when at least one field is set`() {
        viewModel().apply {
            firstNameChanged("First name")
            createProfile()
            events.getOrAwaitValue() shouldBe CreateRATProfileNavigation.ProfileScreen
        }

        verify {
            ratProfileSettings.profile
        }
    }

    @Test
    fun firstNameChanged() {
        viewModel().apply {
            firstNameChanged("First name")
            profile.getOrAwaitValue()!!.apply {
                firstName shouldBe "First name"
                isValid shouldBe true
            }
        }
    }

    @Test
    fun lastNameChanged() {
        viewModel().apply {
            lastNameChanged("Last name")
            profile.getOrAwaitValue()!!.apply {
                lastName shouldBe "Last name"
                isValid shouldBe true
            }
        }
    }

    @Test
    fun birthDateChanged() {
        val birthDate = formatter.parseLocalDate("01.01.2021")
        viewModel().apply {
            birthDateChanged(birthDate)
            profile.getOrAwaitValue()!!.apply {
                birthDate shouldBe birthDate
                isValid shouldBe true
            }
        }
    }

    @Test
    fun streetChanged() {
        viewModel().apply {
            streetChanged("Main St.")
            profile.getOrAwaitValue()!!.apply {
                street shouldBe "Main St."
                isValid shouldBe true
            }
        }
    }

    @Test
    fun zipCodeChanged() {
        viewModel().apply {
            zipCodeChanged("11111")
            profile.getOrAwaitValue()!!.apply {
                zipCode shouldBe "11111"
                isValid shouldBe true
            }
        }
    }

    @Test
    fun cityChanged() {
        viewModel().apply {
            cityChanged("London")
            profile.getOrAwaitValue()!!.apply {
                city shouldBe "London"
                isValid shouldBe true
            }
        }
    }

    @Test
    fun phoneChanged() {
        viewModel().apply {
            phoneChanged("111111111")
            profile.getOrAwaitValue()!!.apply {
                phone shouldBe "111111111"
                isValid shouldBe true
            }
        }
    }

    @Test
    fun emailChanged() {
        viewModel().apply {
            emailChanged("email@example.com")
            profile.getOrAwaitValue()!!.apply {
                email shouldBe "email@example.com"
                isValid shouldBe true
            }
        }
    }

    @Test
    fun allFieldsAreSet() {
        val birthDate = formatter.parseDateTime("01.01.1980").toLocalDate()
        viewModel().apply {
            firstNameChanged("First name")
            lastNameChanged("Last name")
            birthDateChanged(birthDate)
            streetChanged("Main street")
            zipCodeChanged("12132")
            cityChanged("London")
            phoneChanged("111111111")
            emailChanged("email@example.com")
            profile.getOrAwaitValue()!!.apply {
                isValid shouldBe true
                this shouldBe RATProfileData(
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
            events.getOrAwaitValue() shouldBe CreateRATProfileNavigation.Back
        }
    }

    fun viewModel() = RATProfileCreateFragmentViewModel(ratProfileSettings)
}
