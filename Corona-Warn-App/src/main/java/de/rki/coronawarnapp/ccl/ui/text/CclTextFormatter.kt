package de.rki.coronawarnapp.ccl.ui.text

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import dagger.Reusable
import de.rki.coronawarnapp.ccl.configuration.model.cclLanguage
import de.rki.coronawarnapp.ccl.configuration.model.getDefaultInputParameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.CclJsonFunctions
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CclText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Parameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.PluralText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SystemTimeDependentText
import de.rki.coronawarnapp.util.serialization.BaseJackson
import timber.log.Timber
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject

@Reusable
class CclTextFormatter @Inject constructor(
    private val cclJsonFunctions: CclJsonFunctions,
    @BaseJackson private val mapper: ObjectMapper
) {
    /**
     * Format [CclText] based on its sub-types
     * if the text is a [SystemTimeDependentText] it will be evaluated by [CclJsonFunctions]
     * @return [String] empty string if [CclText] is null or could not be formatted
     */
    suspend operator fun invoke(
        cclText: CclText?,
        language: String = cclLanguage,
        locale: Locale = Locale.getDefault()
    ): String = runCatching {
        when (cclText) {
            is PluralText -> cclText.formatPlural(language, locale)
            is SingleText -> cclText.formatSingle(language, locale)
            is SystemTimeDependentText -> invoke(cclText.formatSystemTimeDependent())
            else -> null
        }
    }.getOrElse {
        Timber.w(it, "CclText.format() failed")
        null
    }.orEmpty()

    /**
     * From a url from  FAQ anchor
     * @return url [String] null if anchor is nullable
     */
    operator fun invoke(
        faqAnchor: String?,
        language: String = cclLanguage
    ) = when {
        faqAnchor.isNullOrBlank() -> null
        else -> {
            val languagePath = when (language) {
                Locale.GERMAN.language -> language
                else -> Locale.ENGLISH.language
            }
            "https://www.coronawarn.app/$languagePath/faq/#$faqAnchor"
        }
    }

    private fun SingleText.formatSingle(
        language: String,
        locale: Locale
    ): String? {
        val text = localizedText[language]
            ?: localizedText[EN] // Default for other languages
            ?: localizedText[DE] // Default for EN

        return text?.cleanText()?.format(*parameters.convertValues(locale))
    }

    private fun PluralText.formatPlural(
        language: String,
        locale: Locale
    ): String? {
        val quantity = quantity()
        val quantityText = localizedText[language]
            ?: localizedText[EN] // Default for other languages
            ?: localizedText[DE] // Default for EN
            ?: return null

        val text = pluralText(quantity, quantityText, locale)
        return text.cleanText().format(*parameters.convertValues(locale))
    }

    private fun String.cleanText() = this.replace("%@", "%s")

    private fun PluralText.quantity(): Int = quantity ?: quantityFromIndex()

    private fun PluralText.quantityFromIndex(): Int {
        val param = parameters[quantityParameterIndex ?: 0]
        return when (param.type) {
            Parameters.Type.NUMBER -> param.toNumber()
            else -> error("`quantity` can't be derived from param=$param")
        }
    }

    private suspend fun SystemTimeDependentText.formatSystemTimeDependent(): CclText? =
        runCatching {
            val defaultParameters = getDefaultInputParameters(ZonedDateTime.now()).toObjectNode()
            val allParameters = JsonNodeFactory.instance.objectNode()
                .setAll<ObjectNode>(defaultParameters)
                .setAll<ObjectNode>(parameters)
            val output = cclJsonFunctions.evaluateFunction(functionName, allParameters)
            mapper.treeToValue(output, CclText::class.java)
        }.onFailure {
            Timber.e(it, "SystemTimeDependentText.formatSystemTimeDependent() failed.")
        }.getOrNull()

    private fun List<Parameters>.convertValues(locale: Locale): Array<Any> =
        map { parameter -> parameter.covertValue(locale) }.toTypedArray()

    private fun Parameters.covertValue(locale: Locale) = when (type) {
        Parameters.Type.STRING -> value.toString()
        Parameters.Type.NUMBER -> toNumber()
        Parameters.Type.BOOLEAN -> toBoolean()
        Parameters.Type.LOCAL_DATE -> toLocalDate()
        Parameters.Type.LOCAL_DATE_TIME -> toLocalDateTime()
        Parameters.Type.UTC_DATE -> toUTCDate()
        Parameters.Type.UTC_DATE_TIME -> toUTCDateTime()
    }

    private fun Parameters.toUTCDateTime(): String {
        return runCatching {
            ZonedDateTime.parse(value.toString())
                .withZoneSameInstant(ZoneOffset.UTC).run {
                    "%s, %s".format(
                        format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                        format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
                    )
                }
        }.getOrElse {
            Timber.e(it, "Parameters.toUTCDateTime() failed")
            ""
        }
    }

    private fun Parameters.toUTCDate(): String {
        return runCatching {
            ZonedDateTime.parse(value.toString())
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                )
        }.getOrElse {
            Timber.e(it, "Parameters.toUTCDate() failed")
            ""
        }
    }

    private fun Parameters.toLocalDateTime(): String {
        return runCatching {
            ZonedDateTime.parse(value.toString()).run {
                "%s, %s".format(
                    format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                    format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
                )
            }
        }.getOrElse {
            Timber.e(it, "Parameters.toLocalDateTime() failed")
            ""
        }
    }

    private fun Parameters.toLocalDate(): String {
        return runCatching {
            ZonedDateTime.parse(value.toString()).format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
            )
        }.getOrElse {
            Timber.e(it, "Parameters.toLocalDate() failed")
            ""
        }
    }

    private fun Parameters.toNumber(): Int = runCatching {
        (value as Number).toInt()
    }.getOrElse {
        Timber.e(it, "Parameters.toNumber() failed")
        0
    }

    private fun Parameters.toBoolean(): Boolean = runCatching {
        value as Boolean
    }.getOrElse {
        Timber.e(it, " Parameters.toBoolean() failed")
        false
    }

    private fun Any.toObjectNode(): ObjectNode = mapper.valueToTree(this)

    companion object {
        private const val EN = "en"
        private const val DE = "de"
    }
}
