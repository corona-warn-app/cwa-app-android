package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateQRCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationRepository @Inject constructor(
    @AppScope private val scope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider
) {

    val vaccinationInfos: Flow<Set<VaccinatedPerson>> = emptyFlow()

    suspend fun registerVaccination(
        qrCode: VaccinationCertificateQRCode
    ): VaccinationCertificate {
        throw NotImplementedError()
    }

    fun clear() {
        throw NotImplementedError()
    }

    fun deleteVaccinationCertificate(certificateId: String) =
        scope.launch(context = dispatcherProvider.IO) {
            // TODO delete Vaccination
        }
}
