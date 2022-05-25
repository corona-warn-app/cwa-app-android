package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ValueSetWrapper @Inject constructor(
    valueSetsRepository: ValueSetsRepository,
    dccValidationRepository: DccValidationRepository
) {

    private val countryCodes = dccValidationRepository.dccCountries.map {
        it.map { it.countryCode }
    }

    val valueMap: Flow<Map<String, List<String>>> = combine(
        valueSetsRepository.latestVaccinationValueSets,
        valueSetsRepository.latestTestCertificateValueSets,
        countryCodes
    ) { vaccinationValues, testValues, countryCodes ->
        mapOf(
            COUNTRY to countryCodes,
            DISEASE to vaccinationValues.tg.items.map { it.key },
            "sct-vaccines-covid-19" to vaccinationValues.vp.items.map { it.key },
            "vaccines-covid-19-auth-holders" to vaccinationValues.ma.items.map { it.key },
            "vaccines-covid-19-names" to vaccinationValues.mp.items.map { it.key },
            "covid-19-lab-result" to testValues.tr.items.map { it.key },
            "covid-19-lab-test-manufacturer-and-name" to testValues.ma.items.map { it.key },
            "covid-19-lab-test-type" to testValues.tt.items.map { it.key },
        )
    }
}

private const val DISEASE = "disease-agent-targeted"
private const val COUNTRY = "country-2-codes"
