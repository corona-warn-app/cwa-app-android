@file:Suppress("MaxLineLength")

package de.rki.coronawarnapp.ccl.dccwalletinfo.model

private val boosterNotification = BoosterNotification(
    visible = true,
    titleText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Booster"),
        parameters = listOf()
    ),
    subtitleText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Empfehlung einer Booster-Impfung"),
        parameters = listOf()
    ),
    longText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Die Ständige Impfkommission (STIKO) empfiehlt allen Personen [...]"),
        parameters = listOf()
    ),
    faqAnchor = "dcc_admission_state",
    identifier = "booster_rule_identifier"
)

private val mostRelevantCertificate = Certificate(
    certificateRef = CertificateRef(
        barcodeData = "HC1:6BFOXN*TS0BI\$ZDYSHTRMM7QXSUJCQF*8KF6NDC2LE \$CGH9XYE+9GDJ5RPSDW1:ZH6I1$4JN:IN1MPK95%LS+1VUU8C1VTE5ZMT56QHECDVNE1FO1P56C\$QV/M8YML-1H1V*ZE846N07WW667N+H1YU1E 1PK9B/0E7J0PIBZIXJA  CG8CGZK*9C%PDS1JGDBVF2$7K*IBQQKV-J2 JDZT:1BPMIMIA*NI7UJQWT.+S1QD4007/GJ2HIE9WT0K3M9UVZSVV*001HW%8UE9.955B9-NT0 2$$0X4PPZ0YUIPG1L+N1*PVD4WYHPRAAUICO1-.PU8GIFT383/9TL4T.B9NVP-PELK9D/5.B9C9Q8+ECB9UH9B/9BL5 QE-NEO+11C9P8Q+C9:6V P1HS9UM97H98\$QP3R8BH FRDRIS4FU2STU14G8R6U1YS+FRH8E0IF*+K1D4QIV9BRZX26 L**T -MEUIM3W1KI-\$BEBIU2B4+QF2HFUHBO3E/01.SW:O44BV7QU00W56:0"
    )
)

private val verification = Verification(
    certificates = listOf(
        OutputCertificates(
            buttonText = SingleText(
                type = "string",
                localizedText = mapOf("de" to "2G-Zertifikat"),
                parameters = listOf()
            ),
            certificateRef = CertificateRef(
                barcodeData = "HC1:6BFOXN*TS0BI\$ZDYSHTRMM7QXSUJCQF*8KF6NDC2LE \$CGH9XYE+9GDJ5RPSDW1:ZH6I1$4JN:IN1MPK95%LS+1VUU8C1VTE5ZMT56QHECDVNE1FO1P56C\$QV/M8YML-1H1V*ZE846N07WW667N+H1YU1E 1PK9B/0E7J0PIBZIXJA  CG8CGZK*9C%PDS1JGDBVF2$7K*IBQQKV-J2 JDZT:1BPMIMIA*NI7UJQWT.+S1QD4007/GJ2HIE9WT0K3M9UVZSVV*001HW%8UE9.955B9-NT0 2$$0X4PPZ0YUIPG1L+N1*PVD4WYHPRAAUICO1-.PU8GIFT383/9TL4T.B9NVP-PELK9D/5.B9C9Q8+ECB9UH9B/9BL5 QE-NEO+11C9P8Q+C9:6V P1HS9UM97H98\$QP3R8BH FRDRIS4FU2STU14G8R6U1YS+FRH8E0IF*+K1D4QIV9BRZX26 L**T -MEUIM3W1KI-\$BEBIU2B4+QF2HFUHBO3E/01.SW:O44BV7QU00W56:0"
            )
        ),

        OutputCertificates(
            buttonText = SingleText(
                type = "string",
                localizedText = mapOf("de" to "Testzertifikat"),
                parameters = listOf()
            ),
            certificateRef = CertificateRef(
                barcodeData = "HC1:6BFOXN*TS0BI\$ZDFRH7LHUVNU.8GZ6769MBW3XHP+56R5LLPJ*5PIM*V4DZJIRPQHIZC4.OI:OIG/Q*LP1Z2GHKW/F3IKJ5QH*AF/GJ5MT0K5LO$50D$0SVV88UI*488UZ+3H0LW6WD1KG8U%8W+*3Y:CYZ00OPM98IMIWLHWVHPYH9UE*P1UU1XF820Q R1D.L%:PVLIJRH.OG4SIIRH/R2UZUWM6J$7XLH5G6TH95NI.J2IQ1TU1/PIMJEWT4SS3+Y3D.R9\$H.I8HX7B%R J8VR7* FLVKDN4FTITQI.:KO3HWBJ\$IJQFGSW5B*5:3AUM9NK9:AA945MR9EG3XFJ0%SI23D-I/2DBAJDAJCNB-43 X41Q2EG3RA3/43KD3F23.+IIYC6Q0ZIJPKJ+LJ5AL5:4A93VLJOPG32NQ3QR\$P*NIV1JK%228K*IBYZJ92KENS+7K*8BCIIM7JC0J3LIH.SO4UBR2:ZJ83B4PSZEUP637PS36JF0JEYI1DLZZL1629HQ-A6G 713JIZA213DY4F M52Q0VP25A75Q6TMR V$6L LOOUOPDVSQQR/TTJM:R7:SF90T99K4BTET0J/K%EN6 7I-DRK6*0L03SI30NVB-3"
            )
        )
    )
)

private val vaccinationState = VaccinationState(
    visible = true,
    titleText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Impfstatus"),
        parameters = listOf()
    ),
    subtitleText = PluralText(
        type = "plural",
        quantity = 25,
        localizedText = mapOf(
            "de" to QuantityText(
                zero = "Letzte Impfung heute",
                one = "Letzte Impfung vor %u Tag",
                two = "Letzte Impfung vor %u Tagen",
                few = "Letzte Impfung vor %u Tagen",
                many = "Letzte Impfung vor %u Tagen",
                other = "Letzte Impfung vor %u Tagen"
            )
        ),
        parameters = listOf(
            Parameters(
                type = Parameters.Type.LOCAL_DATE,
                value = "2022-01-01T23:30:00.000Z"
            )
        )
    ),
    longText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Sie haben nun alle derzeit [...]"),
        parameters = listOf()
    ),
    faqAnchor = "dcc_admission_state"
)

private val admissionState = AdmissionState(
    visible = true,
    badgeText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "2G+"),
        parameters = listOf()
    ),
    titleText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Status-Nachweis"),
        parameters = listOf()
    ),
    subtitleText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "2G+ PCR-Test"),
        parameters = listOf()
    ),
    longText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Ihre Zertifikate erfüllen [...]"),
        parameters = listOf()
    ),
    stateChangeNotificationText = SingleText(
        type = "string",
        localizedText = mapOf("de" to "Ihre Zertifikate erfüllen derzeit keine Regel."),
        parameters = listOf()
    ),
    identifier = "admission_state_identifier",
    faqAnchor = "dcc_admission_state"
)

val dccWalletInfo = DccWalletInfo(
    admissionState = admissionState,
    vaccinationState = vaccinationState,
    verification = verification,
    boosterNotification = boosterNotification,
    mostRelevantCertificate = mostRelevantCertificate,
    validUntil = "2022-01-14T18:43:00Z"
)

private val certificateReissuanceLegacy = CertificateReissuance(
    reissuanceDivision = ReissuanceDivision(
        visible = true,
        titleText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Zertifikat ersetzen"),
            parameters = listOf()
        ),
        subtitleText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Text"),
            parameters = listOf()
        ),
        longText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Langer Text"),
            parameters = listOf()
        ),
        faqAnchor = "dcc_admission_state"
    ),
    certificateToReissue = Certificate(
        certificateRef = CertificateRef(
            barcodeData = "HC1:6BFOXN*TS0BI\$ZDYSHTRMM7QXSUJCQF*8KF6NDC2LE \$CGH9XYE+9GDJ5RPSDW1:ZH6I1$4JN:IN1MPK95%LS+1VUU8C1VTE5ZMT56QHECDVNE1FO1P56C\$QV/M8YML-1H1V*ZE846N07WW667N+H1YU1E 1PK9B/0E7J0PIBZIXJA  CG8CGZK*9C%PDS1JGDBVF2$7K*IBQQKV-J2 JDZT:1BPMIMIA*NI7UJQWT.+S1QD4007/GJ2HIE9WT0K3M9UVZSVV*001HW%8UE9.955B9-NT0 2$$0X4PPZ0YUIPG1L+N1*PVD4WYHPRAAUICO1-.PU8GIFT383/9TL4T.B9NVP-PELK9D/5.B9C9Q8+ECB9UH9B/9BL5 QE-NEO+11C9P8Q+C9:6V P1HS9UM97H98\$QP3R8BH FRDRIS4FU2STU14G8R6U1YS+FRH8E0IF*+K1D4QIV9BRZX26 L**T -MEUIM3W1KI-\$BEBIU2B4+QF2HFUHBO3E/01.SW:O44BV7QU00W56:0",
        )
    ),
    accompanyingCertificates = listOf(
        Certificate(
            certificateRef = CertificateRef(
                barcodeData = "HC1:6BFOXN*TS0BI\$ZDYSHTRMM7QXSUJCQF*8KF6NDC2LE \$CGH9XYE+9GDJ5RPSDW1:ZH6I1$4JN:IN1MPK95%LS+1VUU8C1VTE5ZMT56QHECDVNE1FO1P56C\$QV/M8YML-1H1V*ZE846N07WW667N+H1YU1E 1PK9B/0E7J0PIBZIXJA  CG8CGZK*9C%PDS1JGDBVF2$7K*IBQQKV-J2 JDZT:1BPMIMIA*NI7UJQWT.+S1QD4007/GJ2HIE9WT0K3M9UVZSVV*001HW%8UE9.955B9-NT0 2$$0X4PPZ0YUIPG1L+N1*PVD4WYHPRAAUICO1-.PU8GIFT383/9TL4T.B9NVP-PELK9D/5.B9C9Q8+ECB9UH9B/9BL5 QE-NEO+11C9P8Q+C9:6V P1HS9UM97H98\$QP3R8BH FRDRIS4FU2STU14G8R6U1YS+FRH8E0IF*+K1D4QIV9BRZX26 L**T -MEUIM3W1KI-\$BEBIU2B4+QF2HFUHBO3E/01.SW:O44BV7QU00W56:0"
            )
        )
    )
)

private val certificateReissuance = CertificateReissuance(
    reissuanceDivision = ReissuanceDivision(
        visible = true,
        titleText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Zertifikat ersetzen"),
            parameters = listOf()
        ),
        subtitleText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Text"),
            parameters = listOf()
        ),
        longText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Langer Text"),
            parameters = listOf()
        ),
        faqAnchor = "dcc_admission_state",
        identifier = "extend",
        listTitleText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Zu erneuernde Zertifikate:"),
            parameters = listOf()
        ),
        consentSubtitleText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Erneuerung direkt über die App vornehmen"),
            parameters = listOf()
        )
    ),
    certificates = listOf(
        CertificateReissuanceItem(
            certificateToReissue = Certificate(
                certificateRef = CertificateRef(
                    barcodeData = "HC1:6BFOXN*TS0BI\$ZDYSHTRMM7QXSUJCQF*8KF6NDC2LE \$CGH9XYE+9GDJ5RPSDW1:ZH6I1$4JN:IN1MPK95%LS+1VUU8C1VTE5ZMT56QHECDVNE1FO1P56C\$QV/M8YML-1H1V*ZE846N07WW667N+H1YU1E 1PK9B/0E7J0PIBZIXJA  CG8CGZK*9C%PDS1JGDBVF2$7K*IBQQKV-J2 JDZT:1BPMIMIA*NI7UJQWT.+S1QD4007/GJ2HIE9WT0K3M9UVZSVV*001HW%8UE9.955B9-NT0 2$$0X4PPZ0YUIPG1L+N1*PVD4WYHPRAAUICO1-.PU8GIFT383/9TL4T.B9NVP-PELK9D/5.B9C9Q8+ECB9UH9B/9BL5 QE-NEO+11C9P8Q+C9:6V P1HS9UM97H98\$QP3R8BH FRDRIS4FU2STU14G8R6U1YS+FRH8E0IF*+K1D4QIV9BRZX26 L**T -MEUIM3W1KI-\$BEBIU2B4+QF2HFUHBO3E/01.SW:O44BV7QU00W56:0",
                )
            ),
            accompanyingCertificates = listOf(
                Certificate(
                    certificateRef = CertificateRef(
                        barcodeData = "HC1:6BFOXN*TS0BI\$ZDYSHTRMM7QXSUJCQF*8KF6NDC2LE \$CGH9XYE+9GDJ5RPSDW1:ZH6I1$4JN:IN1MPK95%LS+1VUU8C1VTE5ZMT56QHECDVNE1FO1P56C\$QV/M8YML-1H1V*ZE846N07WW667N+H1YU1E 1PK9B/0E7J0PIBZIXJA  CG8CGZK*9C%PDS1JGDBVF2$7K*IBQQKV-J2 JDZT:1BPMIMIA*NI7UJQWT.+S1QD4007/GJ2HIE9WT0K3M9UVZSVV*001HW%8UE9.955B9-NT0 2$$0X4PPZ0YUIPG1L+N1*PVD4WYHPRAAUICO1-.PU8GIFT383/9TL4T.B9NVP-PELK9D/5.B9C9Q8+ECB9UH9B/9BL5 QE-NEO+11C9P8Q+C9:6V P1HS9UM97H98\$QP3R8BH FRDRIS4FU2STU14G8R6U1YS+FRH8E0IF*+K1D4QIV9BRZX26 L**T -MEUIM3W1KI-\$BEBIU2B4+QF2HFUHBO3E/01.SW:O44BV7QU00W56:0"
                    )
                )
            ),
            action = "extend"
        ),
    )
)

val dccWalletInfoWithReissuance = dccWalletInfo.copy(certificateReissuance = certificateReissuance)

val dccWalletInfoWithReissuanceLegacy = dccWalletInfo.copy(certificateReissuance = certificateReissuanceLegacy)

val certificatesRevokedByInvalidationRules = CertificatesRevokedByInvalidationRules(
    certificateRef = CertificateRef(
        barcodeData = "HC1:6BFSW2C9QZPODR3RVJN+1PG0V328D9VRNN0OHXNCX90F2 9JZZ0-KA8+F0MN9UUVDNIHHDBK8KUSILRO1.X8.24BRKGWGX25DNNZ+ELQ2QJTLON*:3MTO92K -D ESIUG-OLAMUX\$S+WPKX5ARM*B87UH8:PCL4TQ79QV1*E+I3:FN-MQMHBF.9-P4UFLXHTR\$9Q%LVYV-TKPLOP2OI%B+3L+\$PW+IJEAP PEAD-J91TAVR9KRTO*LK 3*1K8OF/FU:EJNML/OIDR6ID52V6Q63+YME+GW783LNCZDF6B*89BG6MJKT27 WPJIUACI%31RLO2C2I-PODJ4*93944F29S5/MS91Q2VVK+T-/QPFH4AQRGV82VVYPON1F5FN5498RBCST*QK*NL-USPCQPB4F40OI+OOCQ3UCA8T9.EW6MK43LFRJGLVG.JN4IHCU\$1DYZUE4BWSA+RTW/PNKOJ9N/9J22H\$DVDSGX-R1OA.AMI6N%1CEO5%TGDJR-LM\$YNE9VU/6%PCR-NDFS8:E"

    )
)

val dccWalletInfoWithCertificatesRevokedByInvalidationRules = dccWalletInfo.copy(
    certificatesRevokedByInvalidationRules = listOf(certificatesRevokedByInvalidationRules)
)
