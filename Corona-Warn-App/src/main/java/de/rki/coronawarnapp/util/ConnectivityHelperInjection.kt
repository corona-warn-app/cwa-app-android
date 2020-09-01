package de.rki.coronawarnapp.util

import javax.inject.Inject

// TODO Wrapper that can be removed once **[ConnectivityHelper]** is no longer a singleton
data class ConnectivityHelperInjection @Inject constructor(
    val backgroundPrioritization: BackgroundPrioritization
)
