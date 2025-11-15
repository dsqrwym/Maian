package org.dsqrwym.admin.ui.viewmodels.categories

import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import maian.admin.generated.resources.AdminRes
import maian.admin.generated.resources.category_name_exists
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.create_failed
import maian.shared.generated.resources.create_success
import maian.shared.generated.resources.field_cannot_be_empty
import org.dsqrwym.admin.data.categories.CategoryRepository
import org.dsqrwym.admin.data.categories.dto.CreateCategoryDto
import org.dsqrwym.admin.data.user.UserRepository
import org.dsqrwym.admin.navigation.Categories
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.navigation.core.SharedNavigable
import org.dsqrwym.shared.navigation.core.SharedNavigableDelegate
import org.dsqrwym.shared.network.ErrorMessageMapper
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.formatter.toFixed
import org.dsqrwym.shared.util.validation.sanitizeIvaInput
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kotlin.time.ExperimentalTime

@OptIn(FlowPreview::class)
class CategoriesCreateViewModel(
    categoryRepository: CategoryRepository,
    userRepository: UserRepository,
    mySnackbarViewModel: MySnackbarViewModel
) : BaseCategoryFilterViewmodel(
    userRepository,
    categoryRepository,
    mySnackbarViewModel
), SharedNavigable by SharedNavigableDelegate() {
    // Form state
    private val _categoryName = MutableStateFlow("")
    val categoryName = _categoryName.asStateFlow()
    var isCheckingCategoryName by mutableStateOf(false)
    var categoryNameExist by mutableStateOf(false)
    var categoryNameError by mutableStateOf<StringResource?>(null)
        private set

    var categoryIva by mutableStateOf("")
        private set

    var isPlatformCategory by mutableStateOf(true)
        private set

    var translations = mutableStateListOf<SharedCategoryTranslation>()
        private set

    // UI state
    var createButtonState by mutableStateOf(UiState.Idle)
        private set
    val translationIsValid = derivedStateOf {
        translations.all { it.name.isNotBlank() }
    }
    var createButtonEnabled = derivedStateOf {
        translationIsValid.value
                && createButtonState != UiState.Loading
                && validateCategoryName()
    }
    var showAddLanguageDialog by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            _categoryName.debounce(600)
                .distinctUntilChanged()
                .collectLatest { name ->
                    if (name.isBlank()) return@collectLatest
                    isCheckingCategoryName = true
                    when (val result = categoryRepository.checkCategoryName(name, filterUser?.userId)) {
                        is SharedResponseResult.Success -> {
                            categoryNameExist = result.data == true
                            categoryNameError = if (categoryNameExist) {
                                AdminRes.string.category_name_exists
                            } else {
                                null
                            }
                        }

                        is SharedResponseResult.Error -> {
                            if (ErrorMessageMapper.shouldShowToUser(result.type)) {
                                result.message?.let { mySnackbarViewModel.showError(it) }
                            }
                            categoryNameExist = true
                        }
                    }
                    isCheckingCategoryName = false
                }
        }
    }

    // Update functions
    fun updateCategoryName(name: String) {
        if (categoryName.value.length > 50) return
        _categoryName.value = name
        categoryNameError = if (name.isBlank()) {
            SharedRes.string.field_cannot_be_empty
        } else {
            null
        }
    }

    fun validateCategoryName(): Boolean {
        return !categoryNameExist
                && !isCheckingCategoryName
                && categoryNameError == null
                && categoryName.value.isNotBlank()
    }

    fun showAddLanguageDialog(show: Boolean) {
        showAddLanguageDialog = show
    }

    fun updateCategoryIva(iva: String) {
        val result = sanitizeIvaInput(iva) ?: return
        categoryIva = result
    }

    fun formatIvaTwoDecimal() {
        if (categoryIva.isBlank()) return
        categoryIva = categoryIva.toDoubleOrNull()?.toFixed(2) ?: "0.00"
    }

    fun upsertTranslation(langCode: String, name: String) {
        translations.indexOfFirst { it.langCode == langCode }.let {
            if (it != -1) translations[it] = translations[it].copy(name = name)
            else translations.add(SharedCategoryTranslation(langCode = langCode, name = name))
        }
    }

    fun removeTranslation(langCode: String) {
        translations.find { it.langCode == langCode }?.let { translations.remove(it) }
    }

    @OptIn(ExperimentalTime::class)
    fun createCategory() {
        if (!createButtonEnabled.value) return
        viewModelScope.launch {
            createButtonState = UiState.Loading
            val createDto = CreateCategoryDto(
                name = categoryName.value,
                translations = translations,
                iva = categoryIva.toDoubleOrNull(),
                userId = filterUser?.id,
                parentId = filterParentCategory?.id?.toString(),
            )
            when (val result = categoryRepository.createCategory(createDto)) {
                is SharedResponseResult.Success -> {
                    createButtonState = UiState.Success
                    val message = getString(SharedRes.string.create_success)
                    mySnackbarViewModel.showSuccess(
                        message = message
                    )
                    emitNavigation(NavigationEvent.ToRoute(Categories))
                }

                is SharedResponseResult.Error -> {
                    createButtonState = UiState.Error
                    if (ErrorMessageMapper.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    } else {
                        val message = getString(SharedRes.string.create_failed)
                        mySnackbarViewModel.showError(message)
                    }
                }
            }

            delay(500)
            createButtonState = UiState.Idle
        }

    }

    fun getAvailableLanguages(): List<LanguageManager.SupportedLanguages> {
        return LanguageManager.SupportedLanguages.entries
            .filter { it.code !in translations.map { translation -> translation.langCode } }
    }

    fun toggleCategoryType(it: Boolean) {
        isPlatformCategory = it
        removeParentIdFilter()
    }
}