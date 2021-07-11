package de.rki.coronawarnapp.statistics.local

enum class FederalStateToPackageId(val packageId: Int) {
    BW(1),
    BY(2),
    BE(3),
    BB(3),
    HB(4),
    HH(4),
    HE(7),
    MV(3),
    NI(4),
    NRW(5),
    RP(7),
    SL(7),
    SN(6),
    ST(6),
    SH(4),
    TH(6);

    companion object {
        fun getForName(name: String): FederalStateToPackageId? =
            values().firstOrNull { it.name == name }
    }
}
