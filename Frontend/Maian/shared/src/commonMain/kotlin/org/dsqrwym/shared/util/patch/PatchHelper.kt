package org.dsqrwym.shared.util.patch

import org.dsqrwym.shared.serialization.OptionalField

fun <T> changedField(old: T?, new: T?): OptionalField<T?> {
    return if (old != new) OptionalField.Value(new) else OptionalField.Undefined
}

fun <T> changedFieldNotNull(old: T, new: T): OptionalField<T> {
    return if (old != new) OptionalField.Value(new) else OptionalField.Undefined
}