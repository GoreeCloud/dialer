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
