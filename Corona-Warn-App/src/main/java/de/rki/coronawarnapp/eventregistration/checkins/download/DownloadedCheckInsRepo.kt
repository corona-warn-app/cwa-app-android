package de.rki.coronawarnapp.eventregistration.checkins.download

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject

interface DownloadedCheckInsRepo {

    val allCheckInsPackages: Flow<List<CheckInsPackage>>

    fun addCheckIns(checkins: List<CheckInsPackage>)

    fun removeCheckIns(checkins: List<CheckInsPackage>)
}

class FakeDownloadedCheckInsRepo @Inject constructor() : DownloadedCheckInsRepo {
    override val allCheckInsPackages: Flow<List<CheckInsPackage>>
        get() = listOf(listOf<CheckInsPackage>(FakeCheckInsPackage)).asFlow()

    override fun addCheckIns(checkins: List<CheckInsPackage>) {
        //TODO("Not yet implemented")
    }

    override fun removeCheckIns(checkins: List<CheckInsPackage>) {
        //TODO("Not yet implemented")
    }
}
