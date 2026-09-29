package com.goreecloud.dialer.ui

object DialInputEditor {
    const val MAX_LENGTH = 128

    fun append(current: String, token: String): String {
        require(token in setOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "#", "+")) {
            "Unsupported dial input token."
        }
        if (current.length >= MAX_LENGTH) return current
        if (token == "+" && current.isNotEmpty()) return current
        return (current + token).take(MAX_LENGTH)
    }

    fun replace(raw: String): String {
        val output = StringBuilder()
        raw.forEach { character ->
            when {
                character.isDigit() || character == '*' || character == '#' -> {
                    if (output.length < MAX_LENGTH) output.append(character)
                }

                character == '+' && output.isEmpty() -> {
                    if (output.length < MAX_LENGTH) output.append(character)
                }

                character.isWhitespace() ||
                    character == '-' ||
                    character == '(' ||
                    character == ')' ||
                    character == '.' -> Unit

                else -> Unit
            }
        }
        return output.toString()
    }

    fun deleteLast(current: String): String =
        if (current.isEmpty()) current else current.dropLast(1)

    fun clear(current: String): String =
        if (current.isEmpty()) current else ""
}
