package com.v2ray.ang.enums

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LanguageTest {

    @Test
    fun indonesianIsResolvedFromPickerCode() {
        assertEquals(Language.INDONESIAN, Language.fromCode("in"))
    }

    @Test
    fun indonesianIsResolvedFromModernAndLegacyLanguageTags() {
        assertEquals(Language.INDONESIAN, Language.fromLanguageTag("id"))
        assertEquals(Language.INDONESIAN, Language.fromLanguageTag("in"))
        assertEquals(Language.INDONESIAN, Language.fromLanguageTag("id-ID"))
    }

    @Test
    fun existingLanguagesAreUnchanged() {
        assertEquals(Language.ENGLISH, Language.fromLanguageTag("en"))
        assertEquals(Language.BAKHTIARI, Language.fromCode("bqi-rIR"))
        assertEquals(Language.AUTO, Language.fromLanguageTag("xx"))
    }
}
