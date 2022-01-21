package de.rki.coronawarnapp.ccl.dccwalletinfo.model

// TODO annotate all fields with Jackson annotation
data class DccWalletInfo(
    val admissionState: AdmissionState,
    val vaccinationState: VaccinationState,
    val boosterNotification: BoosterNotification,
    val mostRelevantCertificate: MostRelevantCertificate,
    val verification: Verification,
    val validUntil: String
)

data class AdmissionState(
    val visible: Boolean,
    val badgeText: BadgeText,
    val titleText: TitleText,
    val subtitleText: SubtitleText,
    val longText: LongText,
    val faqAnchor: String
)

data class BadgeText(
    val type: String,
    val localizedText: LocalizedText,
    val parameters: List<String>
)

data class BoosterNotification(

    val visible: Boolean
)

data class ButtonText(

    val type: String,
    val localizedText: LocalizedText,
    val parameters: List<String>
)

data class CertificateRef(

    val barcodeData: String
)

data class Certificates(

    val buttonText: ButtonText,
    val certificateRef: CertificateRef
)

data class PluralText(
    val zero: String,
    val one: String,
    val two: String,
    val few: String,
    val many: String,
    val other: String
)

data class LocalizedText(

    val de: String
)

data class MostRelevantCertificate(

    val certificateRef: CertificateRef
)

data class Parameters(

    val type: String,
    val value: String,
    val format: String,
    val unit: String
)

data class SubtitleText(

    val type: String,
    val quantity: Int,
    val localizedText: LocalizedText,
    val parameters: List<Parameters>
)

data class TitleText(

    val type: String,
    val localizedText: LocalizedText,
    val parameters: List<String>
)

data class VaccinationState(

    val visible: Boolean,
    val titleText: TitleText,
    val subtitleText: SubtitleText,
    val longText: LongText,
    val faqAnchor: String
)

data class Verification(

    val certificates: List<Certificates>
)

data class LongText(

    val type: String,
    val localizedText: LocalizedText,
    val parameters: List<String>
)
