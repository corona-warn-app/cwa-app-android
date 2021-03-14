package de.rki.coronawarnapp.util.network

import android.net.NetworkRequest
import javax.inject.Inject
import javax.inject.Provider

class NetworkRequestBuilderProvider @Inject constructor() : Provider<NetworkRequest.Builder> {
    override fun get(): NetworkRequest.Builder = NetworkRequest.Builder()
}
