package org.dsqrwym.shared.util.validation

import com.sanctionco.jmail.JMail

actual fun validateEmail(email: String): Boolean{
    if (email.isEmpty() || email.isBlank()) return false
    return JMail.isValid(email)
}