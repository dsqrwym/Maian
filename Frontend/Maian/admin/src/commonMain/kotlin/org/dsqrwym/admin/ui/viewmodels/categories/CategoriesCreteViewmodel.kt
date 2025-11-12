package org.dsqrwym.admin.ui.viewmodels.categories

import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
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
import org.jetbrains.compose.resources.StringResource
import kotlin.time.ExperimentalTime

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
    var categoryName by mutableStateOf("")
        private set
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
                && categoryName.isNotBlank()
                && categoryNameError == null
    }
    var showAddLanguageDialog by mutableStateOf(false)
        private set

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

    fun showAddLanguageDialog(show: Boolean) {
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

    fun upsertTranslation(langCode: String, name: String) {
        translations.indexOfFirst { it.langCode == langCode }.let {
            if (it != -1) translations[it] = translations[it].copy(name = name)
            else translations.add(SharedCategoryTranslation(langCode = langCode, name = name))
        }
    }

    fun removeTranslation(langCode: String) {
        translations.find { it.langCode == langCode }?.let { translations.remove(it) }
    }

    // Save function
    @OptIn(ExperimentalTime::class)
    fun createCategory() {
        if (createButtonEnabled.value) {
            viewModelScope.launch {
                createButtonState = UiState.Loading
                val createDto = CreateCategoryDto(
                    name = categoryName,
                    translations = translations,
                    iva = categoryIva.toDoubleOrNull(),
                    userId = filterUser?.id,
                    parentId = filterParentCategory?.id?.toString(),
                )
                when (val result = categoryRepository.createCategory(createDto)) {
                    is SharedResponseResult.Success -> {
                        createButtonState = UiState.Success
                        mySnackbarViewModel.showSuccess(
                            message = "创建成功"
                        )
                        emitNavigation(NavigationEvent.ToRoute(Categories))
                    }

                    is SharedResponseResult.Error -> {
                        createButtonState = UiState.Error
                        if (ErrorMessageMapper.shouldShowToUser(result.type)) {
                            result.message?.let { mySnackbarViewModel.showError(it) }
                        } else {
                            mySnackbarViewModel.showError("创建失败")
                        }
                    }
                }

                delay(500)
                createButtonState = UiState.Idle
            }
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