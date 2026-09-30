package com.goreecloud.dialer.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DialInputEditorTest {
    @Test
    fun internationalPrefixIsAllowedOnlyAtStart() {
        assertEquals("+", DialInputEditor.append("", "+"))
        assertEquals("+1", DialInputEditor.append("+", "1"))
        assertEquals("1", DialInputEditor.append("1", "+"))
    }

    @Test
    fun directEditingAcceptsPhoneCharactersAndDropsFormattingNoise() {
        assertEquals(
            "+12055550199#",
            DialInputEditor.replace("+1 (205) 555-0199#"),
        )
        assertEquals("1205", DialInputEditor.replace("1A2B0C5"))
        assertEquals("+1205", DialInputEditor.replace("++1+205"))
    }

    @Test
    fun directEditingRejectsNonAsciiUnicodeDigits() {
        assertEquals(
            "12",
            DialInputEditor.replace("1\u0662\uFF132"),
        )
    }

    @Test
    fun directEditingIsBounded() {
        assertEquals(
            DialInputEditor.MAX_LENGTH,
            DialInputEditor.replace("1".repeat(DialInputEditor.MAX_LENGTH + 30)).length,
        )
    }

    @Test
    fun deleteAndClearRemainLocalAndDeterministic() {
        assertEquals("12", DialInputEditor.deleteLast("123"))
        assertEquals("", DialInputEditor.deleteLast(""))
        assertEquals("", DialInputEditor.clear("+1 205"))
    }

    @Test
    fun inputIsBounded() {
        val full = "1".repeat(DialInputEditor.MAX_LENGTH)
        assertEquals(full, DialInputEditor.append(full, "2"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unsupportedTokensFailClosed() {
        DialInputEditor.append("", "A")
    }
}
