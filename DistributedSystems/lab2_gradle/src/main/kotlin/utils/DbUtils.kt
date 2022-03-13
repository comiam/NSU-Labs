package utils

import org.postgresql.core.Utils

fun String.escapeQuotes(): String = Utils.escapeLiteral(
    null,
    this,
    true
).toString()