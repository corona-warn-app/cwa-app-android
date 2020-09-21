package de.rki.coronawarnapp.ui.submission.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.ui.submission.SubmissionCountry

class SubmissionCountrySelectViewModel : ViewModel() {
    private val _countries = MutableLiveData<List<SubmissionCountry>>(listOf())
    private val _countriesActive = MediatorLiveData<Boolean>()
    private val _noInfoActive = MediatorLiveData<Boolean>()
    private val _nextActive = MediatorLiveData<Boolean>()

    val countries: LiveData<List<SubmissionCountry>> = _countries
    val countriesActive: LiveData<Boolean> = _countriesActive
    val noInfoActive: LiveData<Boolean> = _noInfoActive
    val nextActive: LiveData<Boolean> = _nextActive

    init {
        _countriesActive.addSource(countries) { countries ->
            _countriesActive.value = countries.any { it.selected }
        }

        _noInfoActive.addSource(countriesActive) {
            if (it) {
                _noInfoActive.value = false
            }
        }

        _nextActive.addSource(countriesActive) {
            if (it) {
                _nextActive.value = true
            }
            if (!it && noInfoActive.value == false) {
                _nextActive.value = false
            }
        }
        _nextActive.addSource(noInfoActive) {
            if (it) {
                _nextActive.value = true
            }
        }
    }

    fun noInfoClick() {
        _countries.value?.let { currentCountries ->
            _countries.postValue(currentCountries.map { it.apply { selected = false } })
        }

        _noInfoActive.postValue(true)
    }

    fun fetchCountries() {
        _countries.postValue(
            listOf(
                SubmissionCountry("IT"),
                SubmissionCountry("ES")
            )
        )
    }

    fun updateCountryCheckedState(country: SubmissionCountry) {
        val newCountries = _countries.value?.map {
            if (it.countryCode == country.countryCode) it.apply {
                selected = country.selected
            } else it
        }
        if (newCountries != null) {
            _countries.postValue(newCountries)
        }
    }
}
