package org.dsqrwym.admin.ui.viewmodels.categories

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.field_cannot_be_empty
import maian.shared.generated.resources.field_required
import org.dsqrwym.admin.data.categories.dto.CategoryDto
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.util.formatter.toFixed
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CategoriesCreateEditViewModel : ViewModel() {

    // Form state
    var categoryName by mutableStateOf("")
        private set
    var categoryNameError by mutableStateOf<StringResource?>(null)
        private set

    var categoryIva by mutableStateOf("")
        private set
    var selectedParentId by mutableStateOf<Long?>(null)
        private set
    var selectedParentError by mutableStateOf<StringResource?>(null)
        private set

    var isPlatformCategory by mutableStateOf(true)
        private set

    var selectedWholesalerId by mutableStateOf<String?>(null)
        private set
    var selectedWholesalerError by mutableStateOf<StringResource?>(null)
        private set

    var translations by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    // UI state
    var createButtonState by mutableStateOf(UiState.Idle)
        private set

    var showAddLanguageDialog by mutableStateOf(false)
        private set

    // Validation
    val nameError by derivedStateOf {
        categoryName.isBlank()
    }

    val wholesalerError by derivedStateOf {
        !isPlatformCategory && selectedWholesalerId == null
    }

    val canSave by derivedStateOf {
        categoryName.isNotBlank() &&
                (isPlatformCategory || selectedWholesalerId != null)
    }

    // Update functions
    fun updateCategoryName(name: String) {
        if (categoryName.length >= 50) return
        categoryName = name
        categoryNameError = if (name.isBlank()) {
            SharedRes.string.field_cannot_be_empty
        } else {
            null
        }
    }

    fun showAddLanguageDialog(show: Boolean){
        showAddLanguageDialog = show
    }

    fun updateCategoryIva(iva: String) {
        if (iva.isBlank()) {
            categoryIva = ""
            return
        }
        val filtered = iva.filter { it.isDigit() || it == '.' }
        val countDot = iva.count { it == '.' }
        if (countDot > 1) return
        val toDouble = filtered.toDoubleOrNull()
        if (toDouble == null || toDouble !in 0.0..100.0) return
        if (toDouble.toString().length > 5) return
        categoryIva = filtered
    }

    fun formatIvaTwoDecimal() {
        if (categoryIva.isBlank()) return
        categoryIva = categoryIva.toDoubleOrNull()?.toFixed(2) ?: "0.00"
    }

    fun selectParentCategory(id: Long?) {
        if (selectedParentId != id) {
            selectedParentId = id
            selectedParentError = null
        }
    }

    fun clearParentCategory() {
        selectedParentId = null
        selectedParentError = null
    }

    fun toggleCategoryType(isPlatform: Boolean) {
        isPlatformCategory = isPlatform
        if (isPlatform) {
            selectedWholesalerId = null
            selectedWholesalerError = null
        }
    }

    fun selectWholesaler(id: String?) {
        selectedWholesalerId = id
        selectedWholesalerError = if (id == null && !isPlatformCategory) {
            SharedRes.string.field_required
        } else {
            null
        }
    }

    fun addTranslation(langCode: String, translation: String = "") {
        translations = translations + (langCode to translation)
    }

    fun updateTranslation(langCode: String, translation: String) {
        translations = translations + (langCode to translation)
    }

    fun removeTranslation(langCode: String) {
        translations = translations - langCode
    }

    fun clearAllTranslations() {
        translations = emptyMap()
    }

    // Validation functions
    private fun validateBasicInfo(): Boolean {
        return categoryName.isNotBlank()
    }

    private fun validateCategoryType(): Boolean {
        return isPlatformCategory || selectedWholesalerId != null
    }

    // Save function
    @OptIn(ExperimentalTime::class)
    fun createCategory() {
        if (!canSave) return

        viewModelScope.launch {
            try {
                createButtonState = UiState.Loading

                // 模拟网络延迟
                kotlinx.coroutines.delay(500)

                val newCategory = CategoryDto(
                    id = Clock.System.now().toEpochMilliseconds(),
                    userId = if (!isPlatformCategory) selectedWholesalerId else null,
                    name = categoryName,
                    iva = categoryIva.toDoubleOrNull(),
                    parentId = selectedParentId,
                    lang = if (translations.isNotEmpty()) translations else null
                )

                createButtonState = UiState.Success

                // 重置表单
                resetForm()

            } catch (e: Exception) {
                createButtonState = UiState.Error
            }
        }
    }

    // Edit mode - load existing category
    fun loadCategory(category: CategoryDto) {
        categoryName = category.name
        categoryIva = category.iva?.toString() ?: ""
        selectedParentId = category.parentId
        isPlatformCategory = category.userId == null
        selectedWholesalerId = category.userId
        translations = category.lang ?: emptyMap()
    }

    // Reset form
    fun resetForm() {
        categoryName = ""
        categoryNameError = null

        categoryIva = ""
        selectedParentId = null
        selectedParentError = null

        isPlatformCategory = true

        selectedWholesalerId = null
        selectedWholesalerError = null

        translations = emptyMap()

        createButtonState = UiState.Idle
    }

    // Get available languages
    fun getAvailableLanguages(): List<LanguageManager.SupportedLanguages> {
        return LanguageManager.SupportedLanguages.entries
            .filter { it.code !in translations.keys }
    }

    // Check if language is already added
    fun isLanguageAdded(langCode: String): Boolean {
        return langCode in translations.keys
    }
}