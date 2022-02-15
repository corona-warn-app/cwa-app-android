package de.rki.coronawarnapp.covidcertificate.person.ui.overview.items

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.ccl.configuration.update.CCLSettings
import de.rki.coronawarnapp.ccl.dccadmission.model.storage.DccAdmissionCheckScenariosRepository
import de.rki.coronawarnapp.ccl.ui.text.CCLTextFormatter
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class AdmissionTileProvider @Inject constructor(
    private val format: CCLTextFormatter,
    appConfigProvider: AppConfigProvider,
    certificatesProvider: PersonCertificatesProvider,
    admissionCheckScenariosRepository: DccAdmissionCheckScenariosRepository,
    cclSettings: CCLSettings,
) {
    val admissionTile = combine(
        admissionCheckScenariosRepository.admissionCheckScenarios,
        appConfigProvider.currentConfig,
        certificatesProvider.personCertificates,
        cclSettings.admissionScenarioId
    ) { admissionScenarios, appConfig, persons, scenarioId ->
        AdmissionTile(
            visible = persons.isNotEmpty() && !appConfig.admissionScenariosDisabled,
            title = format(admissionScenarios?.labelText),
            subtitle = format(
                admissionScenarios?.scenarioSelection?.items?.find { it.identifier == scenarioId }?.titleText
            )
        )
    }

    data class AdmissionTile(
        val visible: Boolean,
        val title: String,
        val subtitle: String
    )
}
