package org.dsqrwym.shared.util.validation

private const val DNI_NIE_CONTROL_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE"
private const val CIF_CONTROL_LETTERS = "JABCDEFGHI"
private val CIF_MUST_BE_DIGIT = setOf('A', 'B', 'E', 'H')
private val CIF_MUST_BE_LETTER = setOf('K', 'P', 'Q', 'S', 'W')

/**
 * 西班牙税号类型
 */
enum class SpanishTaxIdKind {
    DNI, NIE, CIF
}

/**
 * 验证西班牙税号是否合法。
 * 支持：DNI, NIE, CIF / business NIF
 * 输入必须已规范化：大写、无空格、无"-"、无"ES"
 */
fun isValidSpanishTaxId(taxId: String): Boolean {
    return getSpanishTaxIdKind(taxId) != null
}

private fun getSpanishTaxIdKind(taxId: String): SpanishTaxIdKind? {
    val value = taxId.uppercase()

    // DNI: 8 digits + letter
    if (Regex("""^\d{8}[A-Z]$""").matches(value)) {
        return if (isValidDni(value)) SpanishTaxIdKind.DNI else null
    }

    // NIE: X/Y/Z + 7 digits + letter
    if (Regex("""^[XYZ]\d{7}[A-Z]$""").matches(value)) {
        return if (isValidNie(value)) SpanishTaxIdKind.NIE else null
    }

    // CIF: entity letter + 7 digits + control char
    if (Regex("""^[ABCDEFGHJNPQRSUVW]\d{7}[0-9A-J]$""").matches(value)) {
        return if (isValidCif(value)) SpanishTaxIdKind.CIF else null
    }

    return null
}

private fun isValidDni(dni: String): Boolean {
    val number = dni.substring(0, 8).toIntOrNull() ?: return false
    val controlLetter = dni[8]
    return DNI_NIE_CONTROL_LETTERS[number % 23] == controlLetter
}

private fun isValidNie(nie: String): Boolean {
    val first = nie[0]
    val prefix = when (first) {
        'X' -> "0"
        'Y' -> "1"
        'Z' -> "2"
        else -> return false
    }
    val number = (prefix + nie.substring(1, 8)).toIntOrNull() ?: return false
    val controlLetter = nie[8]
    return DNI_NIE_CONTROL_LETTERS[number % 23] == controlLetter
}

private fun isValidCif(cif: String): Boolean {
    val entityLetter = cif[0]
    val digits = cif.substring(1, 8)
    val control = cif[8]

    var sum = 0
    for (i in digits.indices) {
        val n = digits[i].digitToIntOrNull() ?: return false
        if (i % 2 == 0) {
            val doubled = n * 2
            sum += (doubled / 10) + (doubled % 10)
        } else {
            sum += n
        }
    }

    val controlDigit = (10 - (sum % 10)) % 10
    val expectedDigit = controlDigit.toString()[0]
    val expectedLetter = CIF_CONTROL_LETTERS[controlDigit]

    return when {
        CIF_MUST_BE_DIGIT.contains(entityLetter) -> control == expectedDigit
        CIF_MUST_BE_LETTER.contains(entityLetter) -> control == expectedLetter
        else -> control == expectedDigit || control == expectedLetter
    }
}
