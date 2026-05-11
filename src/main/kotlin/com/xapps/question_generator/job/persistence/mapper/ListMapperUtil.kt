package com.xapps.question_generator.job.persistence.mapper

/**
 * Safely joins a list of strings into a single string for persistence.
 * Escapes commas and backslashes so round-tripping is reliable.
 */
fun toPersistedString(list: Collection<String>): String =
    list.joinToString(separator = ",") { it.replace("\\", "\\\\").replace(",", "\\,") }

/**
 * Converts a persisted string back into the original list.
 * Handles escaped commas and backslashes correctly.
 */
fun fromPersistedString(value: String): List<String> {
    if (value.isBlank()) return emptyList()

    val result = mutableListOf<String>()
    val current = StringBuilder()
    var escaping = false

    for (ch in value) {
        when {
            escaping -> {
                current.append(ch)
                escaping = false
            }
            ch == '\\' -> escaping = true
            ch == ',' -> {
                result.add(current.toString())
                current.clear()
            }
            else -> current.append(ch)
        }
    }
    result.add(current.toString())
    return result
}
