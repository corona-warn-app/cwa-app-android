package de.rki.coronawarnapp.ui.submission.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import de.rki.coronawarnapp.ui.submission.SubmissionCountry
import io.kotest.inspectors.forAll
import io.kotest.inspectors.forAtLeastOne
import io.kotest.inspectors.forAtMostOne
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test

class SubmissionCountrySelectViewModelTest {
    @get:Rule
    val instantTaskExecRule = InstantTaskExecutorRule()

    @Test
    fun testFetchCountries() {
        val viewModel = SubmissionCountrySelectViewModel()

        viewModel.fetchCountries()
        // TODO: implement proper test one backend is merged
        viewModel.countries.value!!.size shouldBe 2
    }

    @Test
    fun testUpdateCountryCheckedState() {
        val viewModel = SubmissionCountrySelectViewModel()

        viewModel.fetchCountries()

        viewModel.updateCountryCheckedState(SubmissionCountry("IT", true))
        viewModel.countries.value!!.forAtMostOne {
            it.countryCode shouldBe "IT"
            it.selected shouldBe true
        }

        viewModel.updateCountryCheckedState(SubmissionCountry("IT", false))
        viewModel.countries.value!!.forAtLeastOne {
            it.countryCode shouldBe "IT"
            it.selected shouldBe false
        }
    }

    @Test
    fun testNoInfoClickRemovesSelections() {
        val viewModel = SubmissionCountrySelectViewModel()

        viewModel.fetchCountries()

        viewModel.updateCountryCheckedState(SubmissionCountry("IT", true))
        viewModel.updateCountryCheckedState(SubmissionCountry("ES", true))
        viewModel.countries.value!!.forAll { it.selected shouldBe true }

        viewModel.noInfoClick()

        viewModel.countries.value!!.forAll { it.selected shouldBe false }
    }
}
