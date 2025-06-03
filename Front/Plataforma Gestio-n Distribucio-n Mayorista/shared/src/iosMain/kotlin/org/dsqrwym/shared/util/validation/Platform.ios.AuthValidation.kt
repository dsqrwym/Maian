package org.dsqrwym.shared.util.validation

import platform.Foundation.NSPredicate

actual fun validateEmail(email: String): Boolean {
    val emailRegex = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~\\p{L}\\p{N}-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~\\p{L}\\p{N}-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,64}$"
    val emailTest = NSPredicate.predicateWithFormat("SELF MATCHES %@", emailRegex)

    return emailTest.evaluateWithObject(email)
}