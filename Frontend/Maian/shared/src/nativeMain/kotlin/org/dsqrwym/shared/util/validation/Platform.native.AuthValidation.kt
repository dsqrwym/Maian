package org.dsqrwym.shared.util.validation

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSPredicate
import platform.Foundation.NSString
import platform.Foundation.create

@OptIn(BetaInteropApi::class)
actual fun validateEmail(email: String): Boolean {
    if (email.isBlank()) return false
    val emailRegex = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~\\p{L}\\p{N}-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~\\p{L}\\p{N}-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,64}$"
    val nsRegex = NSString.create(string = emailRegex)
    val emailTest = NSPredicate.predicateWithFormat("SELF MATCHES %@", nsRegex)

    return emailTest.evaluateWithObject(email)
}