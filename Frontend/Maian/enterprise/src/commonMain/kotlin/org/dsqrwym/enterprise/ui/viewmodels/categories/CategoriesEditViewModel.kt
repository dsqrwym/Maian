package org.dsqrwym.enterprise.ui.viewmodels.categories

import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.category_name_exists
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.field_cannot_be_empty
import maian.shared.generated.resources.update_failed
import maian.shared.generated.resources.update_success
import org.dsqrwym.enterprise.data.categories.CategoryRepository
import org.dsqrwym.enterprise.data.categories.dto.UpdateCategoryDto
import org.dsqrwym.enterprise.navigation.Categories
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

@OptIn(FlowPreview::class)
class CategoriesEditViewModel(
    categoryRepository: CategoryRepository,
    mySnackbarViewModel: MySnackbarViewModel
) : BaseCategoryFilterViewmodel(
    categoryRepository,
    mySnackbarViewModel
), SharedNavigable by SharedNavigableDelegate() {
    var isLoading by mutableStateOf(true)
        private set

    // 当前编辑的分类 ID
    private var categoryId: String? = null

    // 初始存在的翻译（用于计算删除列表）
    private val initialLangCodes = mutableStateListOf<String>()

    // 表单：名称
    var categoryName by mutableStateOf("")
        private set
    var categoryIva by mutableStateOf("")
        private set
    var isCheckingCategoryName by mutableStateOf(false)
    var categoryNameExist by mutableStateOf(false)
    var categoryNameError by mutableStateOf<StringResource?>(null)
        private set

    // 翻译
    var translations = mutableStateListOf<SharedCategoryTranslation>()
        private set

    // UI
    var updateButtonState by mutableStateOf(UiState.Idle)
        private set
    val translationIsValid = derivedStateOf {
        translations.all { it.name.isNotBlank() }
    }
    val updateButtonEnabled = derivedStateOf {
        translationIsValid.value
                && updateButtonState != UiState.Loading
                && validateCategoryName() && categoryId != null
    }
    var showAddLanguageDialog by mutableStateOf(false)
        private set

    init {
        // 名称去抖唯一性校验（更新接口）
        viewModelScope.launch {
            snapshotFlow { categoryName }
                .debounce(600)
                .distinctUntilChanged()
                .collectLatest { name ->
                    val id = categoryId ?: return@collectLatest
                    if (name.isBlank()) return@collectLatest
                    isCheckingCategoryName = true
                    when (val result = categoryRepository.checkUpdateCategoryName(name, id)) {
                        is SharedResponseResult.Success -> {
                            categoryNameExist = result.data == true
                            categoryNameError = if (categoryNameExist) {
                                EnterpriseRes.string.category_name_exists
                            } else null
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

    fun initWithCategory(categoryId: String) {
        this@CategoriesEditViewModel.categoryId = categoryId
        viewModelScope.launch {
            when (val result = categoryRepository.getCategoryForUpdate(categoryId)) {
                is SharedResponseResult.Success -> {
                    categoryName = result.data?.name ?: ""
                    result.data?.translations?.let {
                        translations.clear()
                        initialLangCodes.clear()
                        translations.addAll(it)
                        initialLangCodes.addAll(it.map { t -> t.langCode })
                    }

                    isLoading = false
                }

                is SharedResponseResult.Error -> {
                    if (ErrorMessageMapper.shouldShowToUser(result.type)) {
                        result.message?.let {
                            mySnackbarViewModel.showError(it)
                        }
                    }
                    emitNavigation(NavigationEvent.ToRoute(Categories))
                }
            }

        }
    }

    fun updateCategoryName(name: String) {
        val newName = name.take(50)
        categoryName = newName
        categoryNameError = if (newName.isBlank()) {
            SharedRes.string.field_cannot_be_empty
        } else null
    }

    fun validateCategoryName(): Boolean {
        return !categoryNameExist && categoryNameError == null && categoryName.isNotBlank()
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
            if (it != -1) translations[it] = translations[it].copy(name = name.take(50))
            else translations.add(SharedCategoryTranslation(langCode = langCode, name = name.take(50)))
        }
    }

    fun removeTranslation(langCode: String) {
        translations.find { it.langCode == langCode }?.let { translations.remove(it) }
    }

    fun getAvailableLanguages(): List<LanguageManager.SupportedLanguages> {
        return LanguageManager.SupportedLanguages.entries
            .filter { it.code !in translations.map { translation -> translation.langCode } }
    }

    fun submitUpdate() {
        val id = categoryId ?: return
        if (!updateButtonEnabled.value) return
        if (isCheckingCategoryName) return
        viewModelScope.launch {
            updateButtonState = UiState.Loading
            val currentLangCodes = translations.map { it.langCode }.toSet()
            val toDelete = initialLangCodes.filter { it !in currentLangCodes }
            val dto = UpdateCategoryDto(
                id = id,
                name = categoryName,
                iva = categoryIva.toDoubleOrNull(),
                translations = translations,
                translationsToDelete = toDelete.ifEmpty { null }
            )
            when (val result = categoryRepository.updateCategory(dto)) {
                is SharedResponseResult.Success -> {
                    updateButtonState = UiState.Success
                    val message = getString(SharedRes.string.update_success)
                    mySnackbarViewModel.showSuccess(message)
                    emitNavigation(NavigationEvent.ToRoute(Categories))
                }

                is SharedResponseResult.Error -> {
                    updateButtonState = UiState.Error
                    if (ErrorMessageMapper.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    } else {
                        val message = getString(SharedRes.string.update_failed)
                        mySnackbarViewModel.showError(message)
                    }
                }
            }
            delay(500)
            updateButtonState = UiState.Idle
        }
    }
}