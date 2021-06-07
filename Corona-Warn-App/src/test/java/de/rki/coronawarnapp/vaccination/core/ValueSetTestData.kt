package de.rki.coronawarnapp.vaccination.core

object ValueSetTestData {

    val vpItemDe = "1119305005" to "Impfstoff-Name"
    val mpItemDe = "EU/1/21/1529" to "Arzneimittel-Name"
    val maItemDe = "ORG-100001699" to "Hersteller-Name"

    val vpItemEn = vpItemDe.copy(second = "Vaccine-Name")
    val mpItemEn = mpItemDe.copy(second = "MedicalProduct-Name")
    val maItemEn = maItemDe.copy(second = "Manufactorer-Name")
}
