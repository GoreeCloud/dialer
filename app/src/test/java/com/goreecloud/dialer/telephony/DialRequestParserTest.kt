package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DialRequestParserTest {
    @Test
    fun dialWithoutUriOpensEmptyDialRequest() {
        assertEquals(
            DialRequest(number = null),
            DialRequestParser.parse(DialRequestParser.ACTION_DIAL, null, null),
        )
    }

    @Test
    fun telDialPreservesSuppliedAddress() {
        assertEquals(
            DialRequest(number = "+12025550123"),
            DialRequestParser.parse(
                DialRequestParser.ACTION_DIAL,
                "tel",
                "+12025550123",
            ),
        )
    }

    @Test
    fun nonDialActionIsIgnored() {
        assertNull(DialRequestParser.parse("android.intent.action.VIEW", "tel", "123"))
    }

    @Test
    fun unsupportedSchemeIsIgnored() {
        assertNull(DialRequestParser.parse(DialRequestParser.ACTION_DIAL, "https", "example.com"))
    }
}
