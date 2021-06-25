package de.rki.coronawarnapp.covidcertificate.validationrules.rules

import org.joda.time.Instant
import org.json.JSONObject

interface DgcValidationRule {
    // Unique identifier of the rule "GR-CZ-0001"
    val identifier: String

    // Type of the rule ("Acceptance", "Invalidation")
    val type: ValidationRuleType

    // Country code of the country that created the rule
    // ISO 3166 two-letter country code "Country": "CZ",
    val country: String

    // Version of the rule "Version": "1.0.0",
    val version: String

    // Version of the DGC Schema that this rule applies to "SchemaVersion": "1.0.0",
    val schemaVersion: String

    // Rule engine "CERTLOGIC"
    val engine: String

    // Rule engine version "1.0.0"
    val engineVersion: String

    // DCC type
    // (General, Test, Vaccination, Recovery)
    val certificateType: String

    // Description by language
    // [ {"lang": "en","desc": "The Field “Doses” MUST contain number 2 OR 2/2."} ]
    val description: List<Map<String, String>>

    // Start and end of validity period
    // ISO 8106 date-time  "2021-05-27T07:46:40Z"
    val validFrom: Instant
    val validTo: Instant

    // Fields affected by the rule [ "dn", "sd" ]
    val affectedFields: List<String>

    // CertLogic rule as JSON object
    //  { "and":[{ ">":[{ "var":"hcert.v.0.dn" }, 0] },{ ">=":[{ "var":"hcert.v.0.dn" },{ "var":"hcert.v.0.sd" }] }]}
    val logic: JSONObject
}
