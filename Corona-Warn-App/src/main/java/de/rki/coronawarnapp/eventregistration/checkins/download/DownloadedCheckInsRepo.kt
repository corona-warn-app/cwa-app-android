package de.rki.coronawarnapp.eventregistration.checkins.download

import kotlinx.coroutines.flow.Flow

interface DownloadedCheckInsRepo {

    val allCheckInsPackages: Flow<List<CheckInsPackage>>

    fun addCheckIns(checkins: List<CheckInsPackage>)

    fun removeCheckIns(checkins: List<CheckInsPackage>)
}
