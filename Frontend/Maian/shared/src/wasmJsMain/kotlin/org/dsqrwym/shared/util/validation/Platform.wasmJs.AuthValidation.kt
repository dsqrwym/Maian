package org.dsqrwym.shared.util.validation

import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement

actual fun validateEmail(email: String): Boolean {
    if (email.isEmpty() || email.isBlank()) return false
    val input = document.createElement("input") as HTMLInputElement
    input.type = "email"
    input.value = email
    return input.checkValidity()
}