package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.util.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ValueSetWrapper @Inject constructor(
    valueSetsRepository: ValueSetsRepository,
    private val dccValidationRepository: DccValidationRepository
) {

    private val countryCodes = dccValidationRepository.dccCountries.map {
        it.map { it.countryCode }
    }

    val valueSetVaccination: Flow<Map<String, List<String>>> = combine(
        valueSetsRepository.latestVaccinationValueSets,
        countryCodes
    ) { vaccinationValues, countryCodes ->
        mapOf(
            COUNTRY to countryCodes,
            DISEASE to vaccinationValues.tg.items.map { it.key },
            "sct-vaccines-covid-19" to vaccinationValues.vp.items.map { it.key },
            "vaccines-covid-19-auth-holders " to vaccinationValues.ma.items.map { it.key },
            "vaccines-covid-19-names" to vaccinationValues.mp.items.map { it.key },
        )
    }

    val valueSetTest: Flow<Map<String, List<String>>> = combine(
        valueSetsRepository.latestTestCertificateValueSets,
        countryCodes
    ) { testValues, countryCodes ->
        mapOf(
            COUNTRY to countryCodes,
            DISEASE to testValues.tg.items.map { it.key },
            "covid-19-lab-result" to testValues.tr.items.map { it.key },
            "covid-19-lab-test-manufacturer-and-name" to testValues.ma.items.map { it.key },
            "covid-19-lab-test-type" to testValues.tt.items.map { it.key },
        )
    }

    val valueSetRecovery: Flow<Map<String, List<String>>> = combine(
        valueSetsRepository.latestVaccinationValueSets,
        countryCodes
    ) { vaccinationValues, countryCodes ->
        mapOf(
            COUNTRY to countryCodes,
            DISEASE to vaccinationValues.tg.items.map { it.key },
        )
    }
}

private const val DISEASE = "disease-agent-targeted"
private const val COUNTRY = "country-2-codes"
