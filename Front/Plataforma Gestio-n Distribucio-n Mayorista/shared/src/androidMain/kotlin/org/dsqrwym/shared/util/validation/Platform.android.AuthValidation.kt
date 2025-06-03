package org.dsqrwym.shared.util.validation

import com.sanctionco.jmail.JMail

actual fun validateEmail(email: String): Boolean{
    return JMail.isValid(email)
}