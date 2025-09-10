package org.dsqrwym.shared.util.validation

import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement

actual fun validateEmail(email: String): Boolean {
    val input = document.createElement("input") as HTMLInputElement
    input.type = "email"
    input.value = email
    return input.checkValidity()
}