package de.rki.coronawarnapp.covidcertificate.signature.core

import de.rki.coronawarnapp.server.protocols.internal.dgc.DscListOuterClass.DscListItem

data class DscData(
    val dscList: List<DscListItem>
)
