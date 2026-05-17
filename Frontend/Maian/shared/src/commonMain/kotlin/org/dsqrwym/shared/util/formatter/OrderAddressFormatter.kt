package org.dsqrwym.shared.util.formatter

import org.dsqrwym.shared.data.orders.dto.SharedOrderShippingAddressSnapshot

fun SharedOrderShippingAddressSnapshot.toSpanishAddressFormat(): String =
    listOfNotNull(
        street.notBlankOrNull(),
        listOfNotNull(zipCode.notBlankOrNull(), displayCityName()).joinToString(" ").notBlankOrNull(),
        displayProvinceName(),
        displayCountryName(),
    ).joinToString(", ")

fun SharedOrderShippingAddressSnapshot.displayCityName(): String? =
    cityNameLocal.notBlankOrNull() ?: cityName.notBlankOrNull()

fun SharedOrderShippingAddressSnapshot.displayProvinceName(): String? =
    provinceNameLocal.notBlankOrNull() ?: provinceName.notBlankOrNull()

fun SharedOrderShippingAddressSnapshot.displayCountryName(): String? =
    countryNameLocal.notBlankOrNull()
        ?: countryName.notBlankOrNull()
        ?: countryAlpha2.notBlankOrNull()

fun String?.notBlankOrNull(): String? =
    this?.trim()?.takeIf { it.isNotBlank() }
