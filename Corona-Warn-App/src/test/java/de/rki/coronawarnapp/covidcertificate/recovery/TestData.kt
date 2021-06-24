package de.rki.coronawarnapp.covidcertificate.recovery

import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.util.serialization.fromJson

val json =
    """{ "ver": "1.2.1", "nam": { "fn": "Musterfrau-G\u00f6\u00dfinger", "gn": "Gabriele", "fnt": "MUSTERFRAU<GOESSINGER", "gnt": "GABRIELE" }, "dob": "1998-02-26", "r": [ { "tg": "840539006", "fr": "2021-02-20", "co": "AT", "is": "Ministry of Health, Austria", "df": "2021-04-04", "du": "2021-10-04", "ci": "URN:UVCI:01:AT:858CC18CFCF5965EF82F60E493349AA5#K" } ] }"""
val recoveryCertificate1 = Gson().fromJson<DccV1>(json)
