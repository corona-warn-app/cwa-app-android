package de.rki.coronawarnapp.covidcertificate.signature.core

import dagger.Reusable
import de.rki.coronawarnapp.server.protocols.internal.dgc.DscListOuterClass
import de.rki.coronawarnapp.util.toOkioByteString
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class DscDataParser @Inject constructor() {

    fun parse(rawData: ByteArray, updatedAt: Instant = Instant.now()): DscSignatureList = DscSignatureList(
        dscList = DscListOuterClass.DscList.parseFrom(rawData).certificatesList.map {
            DscItem(
                kid = it.kid.toOkioByteString().base64(),
                data = it.data.toOkioByteString()
            )
        },
        updatedAt = updatedAt
    )
}
