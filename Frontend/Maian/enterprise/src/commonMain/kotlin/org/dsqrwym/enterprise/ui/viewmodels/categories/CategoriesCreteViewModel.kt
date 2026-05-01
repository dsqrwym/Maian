package org.dsqrwym.enterprise.ui.viewmodels.categories

import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.category_name_exists
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.create_failed
import maian.shared.generated.resources.create_success
import maian.shared.generated.resources.field_cannot_be_empty
import org.dsqrwym.business.navigation.Categories
import org.dsqrwym.enterprise.data.category.CategoryRepository
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.navigation.core.SharedNavigable
import org.dsqrwym.shared.navigation.core.SharedNavigableDelegate
import org.dsqrwym.shared.network.mapper.ErrorMessageMapper
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.formatter.toFixed
import org.dsqrwym.shared.util.validation.sanitizeIvaInput
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(FlowPreview::class)
class CategoriesCreateViewModel(
    categoryRepository: CategoryRepository,
    mySnackbarViewModel: MySnackbarViewModel
) : BaseCategoryFilterViewmodel(
    categoryRepository,
    mySnackbarViewModel
), SharedNavigable by SharedNavigableDelegate() {
    // Form state
    var categoryName by mutableStateOf("")
        private set
    var isCheckingCategoryName by mutableStateOf(false)
    var categoryNameExist by mutableStateOf(false)
    var categoryNameError by mutableStateOf<StringResource?>(null)
        private set

    var categoryIva: String? by mutableStateOf("21")
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
            snapshotFlow { categoryName }
                .debounce(600.milliseconds)
                .distinctUntilChanged()
                .collectLatest { name ->
                    if (name.isBlank()) return@collectLatest
                    isCheckingCategoryName = true
                    when (val result = categoryRepository.checkCategoryName(name)) {
                        is SharedResponseResult.Success -> {
                            categoryNameExist = result.data == true
                            categoryNameError = if (categoryNameExist) {
                                BusinessRes.string.category_name_exists
                            } else {
                                null
                            }
                        }

                        is SharedResponseResult.Error -> {
                            if (SharedResponseResult.shouldShowToUser(result.type)) {
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
        val newName = name.take(50)
        categoryName = newName
        categoryNameError = if (newName.isBlank()) {
            SharedRes.string.field_cannot_be_empty
        } else {
            null
        }
    }

    fun validateCategoryName(): Boolean {
        return !categoryNameExist
                && categoryNameError == null
                && categoryName.isNotBlank()
    }

    fun showAddLanguageDialog(show: Boolean) {
        showAddLanguageDialog = show
    }

    fun updateCategoryIva(iva: String?) {
        val result = iva?.let { sanitizeIvaInput(iva) }
        categoryIva = result
    }

    fun formatIvaTwoDecimal() {
        categoryIva = categoryIva?.toDoubleOrNull()?.toFixed(2)
    }

    fun upsertTranslation(langCode: String, name: String) {
        translations.indexOfFirst { it.langCode == langCode }.let {
            if (it != -1) translations[it] = translations[it].copy(name = name.take(50))
            else translations.add(SharedCategoryTranslation(langCode = langCode, name = name.take(50)))
        }
    }

    fun removeTranslation(langCode: String) {
        translations.find { it.langCode == langCode }?.let { translations.remove(it) }
    }

    @OptIn(ExperimentalTime::class)
    fun createCategory() {
        if (!createButtonEnabled.value) return
        if (isCheckingCategoryName) return
        viewModelScope.launch {
            createButtonState = UiState.Loading
            when (val result = categoryRepository.createCategory(
                categoryName,
                categoryIva,
                filterCategory?.id,
                translations
            )) {
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
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    } else {
                        val message = getString(SharedRes.string.create_failed)
                        mySnackbarViewModel.showError(message)
                    }
                }
            }

            delay(500.milliseconds)
            createButtonState = UiState.Idle
        }

    }

    fun getAvailableLanguages(): List<LanguageManager.SupportedLanguages> {
        return LanguageManager.SupportedLanguages.entries
            .filter { it.code !in translations.map { translation -> translation.langCode } }
    }
}
