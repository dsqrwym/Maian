package org.dsqrwym.shared.ui.viewmodels.address

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.field_cannot_be_empty
import maian.shared.generated.resources.field_required
import org.dsqrwym.shared.data.location.SharedLocationRepository
import org.dsqrwym.shared.data.location.dto.CityDto
import org.dsqrwym.shared.data.location.dto.CountryDto
import org.dsqrwym.shared.data.location.dto.DirectionPatchRequest
import org.dsqrwym.shared.data.location.dto.ProvinceDto
import org.dsqrwym.shared.data.profile.StoreDirectionDto
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.serialization.OptionalField
import org.jetbrains.compose.resources.StringResource

class SharedAddressFormState(
    private val locationRepository: SharedLocationRepository,
    private val scope: CoroutineScope,
) {
    var street by mutableStateOf("")
        private set
    var streetError by mutableStateOf<StringResource?>(null)
        private set
    var zipCode by mutableStateOf("")
        private set
    var zipCodeError by mutableStateOf<StringResource?>(null)
        private set

    var countries by mutableStateOf<List<CountryDto>>(emptyList())
        private set
    var provinces by mutableStateOf<List<ProvinceDto>>(emptyList())
        private set
    var cities by mutableStateOf<List<CityDto>>(emptyList())
        private set

    var selectedCountryIso by mutableStateOf<Int?>(null)
        private set
    var selectedCountryError by mutableStateOf<StringResource?>(null)
        private set
    var selectedProvinceId by mutableStateOf<Int?>(null)
        private set
    var selectedProvinceError by mutableStateOf<StringResource?>(null)
        private set
    var selectedCityId by mutableStateOf<Int?>(null)
        private set
    var selectedCityError by mutableStateOf<StringResource?>(null)
        private set

    var isLoadingCountries by mutableStateOf(false)
        private set
    var isLoadingProvinces by mutableStateOf(false)
        private set
    var isLoadingCities by mutableStateOf(false)
        private set

    fun populate(direction: StoreDirectionDto?) {
        street = direction?.street.orEmpty()
        streetError = null
        zipCode = direction?.zipCode.orEmpty()
        zipCodeError = null

        selectedCountryIso = direction?.country?.isoNumeric
        selectedCountryError = null
        selectedProvinceId = direction?.province?.id
        selectedProvinceError = null
        selectedCityId = direction?.city?.id
        selectedCityError = null

        countries = direction?.country?.let { listOf(it) } ?: emptyList()
        provinces = direction?.province?.let { listOf(it) } ?: emptyList()
        cities = direction?.city?.let { listOf(it) } ?: emptyList()

        scope.launch {
            loadCountries()
            selectedCountryIso?.let { loadProvinces(it) }
            selectedProvinceId?.let { loadCities(it) }
        }
    }

    fun updateStreet(value: String) {
        val next = value.take(200)
        street = next
        streetError = if (next.isBlank() && hasInput()) SharedRes.string.field_cannot_be_empty else null
    }

    fun updateZipCode(value: String) {
        val next = value.take(10)
        zipCode = next
        zipCodeError = if (next.isBlank() && hasInput()) SharedRes.string.field_cannot_be_empty else null
    }

    fun selectCountry(isoNumeric: Int?) {
        if (selectedCountryIso == isoNumeric) return

        selectedCountryIso = isoNumeric
        selectedProvinceId = null
        selectedCityId = null
        provinces = emptyList()
        cities = emptyList()

        selectedCountryError = if (isoNumeric == null && hasInput()) SharedRes.string.field_required else null
        selectedProvinceError = null
        selectedCityError = null

        isoNumeric?.let { scope.launch { loadProvinces(it) } }
    }

    fun selectProvince(id: Int?) {
        if (selectedProvinceId == id) return

        selectedProvinceId = id
        selectedCityId = null
        cities = emptyList()

        selectedProvinceError = if (id == null && hasInput()) SharedRes.string.field_required else null
        selectedCityError = null

        id?.let { scope.launch { loadCities(it) } }
    }

    fun selectCity(id: Int?) {
        if (selectedCityId == id) return

        selectedCityId = id
        selectedCityError = if (id == null && hasInput()) SharedRes.string.field_required else null
    }

    fun ensureCountriesLoaded() {
        if (countries.isEmpty() && !isLoadingCountries) {
            scope.launch { loadCountries() }
        }
    }

    fun validateForSave(initial: StoreDirectionDto?): Boolean {
        if (!requiresCompleteAddress(initial)) {
            clearErrors()
            return true
        }

        streetError = if (normalizedStreet() == null) SharedRes.string.field_cannot_be_empty else null
        zipCodeError = if (normalizedZipCode() == null) SharedRes.string.field_cannot_be_empty else null
        selectedCountryError = if (selectedCountryIso == null) SharedRes.string.field_required else null
        selectedProvinceError = if (selectedProvinceId == null) SharedRes.string.field_required else null
        selectedCityError = if (selectedCityId == null) SharedRes.string.field_required else null

        return isValidForSave(initial)
    }

    fun isValidForSave(initial: StoreDirectionDto?): Boolean {
        if (!requiresCompleteAddress(initial)) return true

        return normalizedStreet() != null &&
                normalizedZipCode() != null &&
                selectedCountryIso != null &&
                selectedProvinceId != null &&
                selectedCityId != null &&
                streetError == null &&
                zipCodeError == null &&
                selectedCountryError == null &&
                selectedProvinceError == null &&
                selectedCityError == null
    }

    fun hasChanged(initial: StoreDirectionDto?): Boolean {
        return snapshot() != initial.snapshot()
    }

    fun changedDirectionField(initial: StoreDirectionDto?): OptionalField<DirectionPatchRequest> {
        val request = toDirectionPatchOrNull(initial) ?: return OptionalField.Undefined
        return OptionalField.Value(request)
    }

    private suspend fun loadCountries() {
        isLoadingCountries = true
        countries = when (val result = locationRepository.getCountries()) {
            is SharedResponseResult.Success -> result.data?.takeIf { it.isNotEmpty() } ?: countries
            is SharedResponseResult.Error -> countries
        }
        isLoadingCountries = false
    }

    private suspend fun loadProvinces(isoNumeric: Int) {
        isLoadingProvinces = true
        provinces = when (val result = locationRepository.getProvincesByCountry(isoNumeric)) {
            is SharedResponseResult.Success -> result.data?.takeIf { it.isNotEmpty() } ?: provinces
            is SharedResponseResult.Error -> provinces
        }
        isLoadingProvinces = false
    }

    private suspend fun loadCities(provinceId: Int) {
        isLoadingCities = true
        cities = when (val result = locationRepository.getCitiesByProvince(provinceId)) {
            is SharedResponseResult.Success -> result.data?.takeIf { it.isNotEmpty() } ?: cities
            is SharedResponseResult.Error -> cities
        }
        isLoadingCities = false
    }

    private fun toDirectionPatchOrNull(initial: StoreDirectionDto?): DirectionPatchRequest? {
        val current = snapshot()
        val original = initial.snapshot()
        if (current == original) return null

        return DirectionPatchRequest(
            street = current.street.takeIf { it != original.street },
            zipCode = current.zipCode.takeIf { it != original.zipCode },
            country = current.country.takeIf { it != original.country },
            province = current.province.takeIf { it != original.province },
            city = current.city.takeIf { it != original.city },
        )
    }

    private fun hasInput(): Boolean {
        return street.isNotBlank() ||
                zipCode.isNotBlank() ||
                selectedCountryIso != null ||
                selectedProvinceId != null ||
                selectedCityId != null
    }

    private fun requiresCompleteAddress(initial: StoreDirectionDto?): Boolean {
        return initial.hasAddress() || hasInput()
    }

    private fun clearErrors() {
        streetError = null
        zipCodeError = null
        selectedCountryError = null
        selectedProvinceError = null
        selectedCityError = null
    }

    private fun normalizedStreet(): String? = street.trim().takeIf { it.isNotBlank() }

    private fun normalizedZipCode(): String? = zipCode.trim().takeIf { it.isNotBlank() }

    private fun snapshot(): AddressSnapshot = AddressSnapshot(
        street = normalizedStreet(),
        zipCode = normalizedZipCode(),
        country = selectedCountryIso,
        province = selectedProvinceId,
        city = selectedCityId,
    )

    private fun StoreDirectionDto?.hasAddress(): Boolean = this.snapshot().hasAnyValue()

    private fun StoreDirectionDto?.snapshot(): AddressSnapshot = AddressSnapshot(
        street = this?.street?.trim()?.takeIf { it.isNotBlank() },
        zipCode = this?.zipCode?.trim()?.takeIf { it.isNotBlank() },
        country = this?.country?.isoNumeric,
        province = this?.province?.id,
        city = this?.city?.id,
    )

    private data class AddressSnapshot(
        val street: String?,
        val zipCode: String?,
        val country: Int?,
        val province: Int?,
        val city: Int?,
    ) {
        fun hasAnyValue(): Boolean {
            return street != null || zipCode != null || country != null || province != null || city != null
        }
    }
}
