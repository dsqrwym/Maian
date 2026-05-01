package org.dsqrwym.admin.ui.viewmodels.categories

import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import io.ktor.http.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.category_name_exists
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.field_cannot_be_empty
import maian.shared.generated.resources.update_failed
import maian.shared.generated.resources.update_success
import org.dsqrwym.admin.data.categories.CategoryRepository
import org.dsqrwym.admin.data.user.UserRepository
import org.dsqrwym.business.data.category.dto.BusinessCategoryForUpdateResponseDto
import org.dsqrwym.business.data.category.dto.BusinessUpdateCategoryDto
import org.dsqrwym.business.navigation.Categories
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.navigation.core.SharedNavigable
import org.dsqrwym.shared.navigation.core.SharedNavigableDelegate
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.serialization.OptionalField
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.formatter.toFixed
import org.dsqrwym.shared.util.validation.sanitizeIvaInput
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class CategoriesEditViewModel(
    categoryRepository: CategoryRepository,
    userRepository: UserRepository,
    mySnackbarViewModel: MySnackbarViewModel
) : BaseCategoryFilterViewmodel(
    userRepository,
    categoryRepository,
    mySnackbarViewModel
), SharedNavigable by SharedNavigableDelegate() {
    var isLoading by mutableStateOf(true)
        private set

    // 当前编辑的分类 ID
    private var categoryId: String? = null

    private val initialLangCodes = mutableStateListOf<String>()

    // 表单：名称
    var categoryName by mutableStateOf("")
        private set
    var categoryIva: String? by mutableStateOf(null)
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
        if (translations.isEmpty()) return@derivedStateOf true
        translations.all { it.name.isNotBlank() }
    }
    val updateButtonEnabled = derivedStateOf {
        translationIsValid.value
                && updateButtonState != UiState.Loading
                && validateCategoryName() && categoryId != null
                && !isCheckingCategoryName
    }
    var showAddLanguageDialog by mutableStateOf(false)
        private set

    // 更新

    private var initialCategory: BusinessCategoryForUpdateResponseDto? = null
    private var categoryVersion: Long = 0L

    // 辅助属性：判断各字段是否被用户修改
    private val isNameChanged: Boolean
        get() = categoryName != initialCategory?.name

    private val isIvaChanged: Boolean
        get() = categoryIva != initialCategory?.iva

    private val isTranslationsChanged: Boolean
        get() = !areTranslationListsEqual(translations, initialCategory?.translations)

    // 比较两个翻译列表是否相同（忽略顺序，按 langCode 比较内容）
    private fun areTranslationListsEqual(
        current: List<SharedCategoryTranslation>,
        latest: List<SharedCategoryTranslation>?
    ): Boolean {
        if (latest == null) return current.isEmpty()
        return current.toSet() == latest.toSet()
    }

    // 计算需要删除的 langCode（基于初始值）
    private val translationsToDelete: List<String>
        get() {
            val initialCodes = initialCategory?.translations?.map { it.langCode } ?: emptyList()
            val currentCodes = translations.map { it.langCode }.toSet()
            return initialCodes.filter { it !in currentCodes }
        }


    init {
        // 名称去抖唯一性校验（更新接口）
        viewModelScope.launch {
            snapshotFlow { categoryName }.debounce(600.milliseconds)
                .distinctUntilChanged()
                .collectLatest { name ->
                    val id = categoryId ?: return@collectLatest
                    if (name.isBlank()) return@collectLatest
                    isCheckingCategoryName = true
                    when (val result = categoryRepository.checkUpdateCategoryName(name, id, filterUser?.userId)) {
                        is SharedResponseResult.Success -> {
                            categoryNameExist = result.data == true
                            categoryNameError = if (categoryNameExist) {
                                BusinessRes.string.category_name_exists
                            } else null
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

    fun initWithCategory(categoryId: String) {
        resetViewModel()
        this@CategoriesEditViewModel.categoryId = categoryId
        isLoading = true
        viewModelScope.launch {
            when (val result = categoryRepository.getCategoryForUpdate(categoryId)) {
                is SharedResponseResult.Success -> {
                    result.data?.let { data ->
                        initialCategory = data  // 保存原始数据快照
                        categoryName = data.name ?: ""

                        translations.clear()
                        data.translations?.let {
                            translations.addAll(it)
                        }

                        categoryIva = data.iva
                        categoryVersion = data.version
                    }
                }

                is SharedResponseResult.Error -> {
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let {
                            mySnackbarViewModel.showError(it)
                        }
                    }
                    emitNavigation(NavigationEvent.ToRoute(Categories))
                }
            }
            isLoading = false
        }
    }

    fun updateCategoryName(name: String) {
        isCheckingCategoryName = true
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
            val dto = BusinessUpdateCategoryDto(
                name = if (isNameChanged) {
                    OptionalField.Value(categoryName)
                } else {
                    OptionalField.Undefined
                },
                iva = if (isIvaChanged) {
                    categoryIva?.let { OptionalField.Value(it) }
                } else {
                    OptionalField.Undefined
                },
                translations = if (isTranslationsChanged) {
                    OptionalField.Value(translations)
                } else {
                    OptionalField.Undefined
                },
                translationsToDelete = translationsToDelete.takeIf { it.isNotEmpty() }?.let {
                    OptionalField.Value(it)
                } ?: OptionalField.Undefined,
                version = categoryVersion
            )
            when (val result = categoryRepository.updateCategory(id, dto)) {
                is SharedResponseResult.Success -> {
                    updateButtonState = UiState.Success
                    val message = getString(SharedRes.string.update_success)
                    mySnackbarViewModel.showSuccess(message)
                    emitNavigation(NavigationEvent.ToRoute(Categories))
                }

                is SharedResponseResult.Error -> {
                    updateButtonState = UiState.Error
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        if (result.type == HttpStatusCode.Conflict) {
                            handleConflictAndMerge()
                        }
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    } else {
                        val message = getString(SharedRes.string.update_failed)
                        mySnackbarViewModel.showError(message)
                    }
                }
            }
            delay(500.milliseconds)
            updateButtonState = UiState.Idle
        }
    }

    private fun resetViewModel() {
        categoryName = ""
        categoryNameError = null
        categoryIva = null
        categoryNameError = null
        categoryId = null
        categoryNameExist = false
        initialLangCodes.clear()
        isCheckingCategoryName = false
        translations.clear()
    }

    private suspend fun handleConflictAndMerge() {
        val currentId = categoryId ?: return

        when (val fetchResult = categoryRepository.getCategoryForUpdate(currentId)) {
            is SharedResponseResult.Error -> {
                if (SharedResponseResult.shouldShowToUser(fetchResult.type)) {
                    fetchResult.message?.let {
                        mySnackbarViewModel.showError(it)
                    }
                }
                emitNavigation(NavigationEvent.ToRoute(Categories))

            }

            is SharedResponseResult.Success -> {
                val latest = fetchResult.data ?: return
                val oldInitial = initialCategory ?: return

                // 用户主动删除的语言
                val deletedLangCodes = translationsToDelete.toSet()

                val mergedName = if (isNameChanged) categoryName else latest.name ?: ""
                val mergedIva = if (isIvaChanged) categoryIva else latest.iva

                // 构建旧数据映射
                val oldTransMap = oldInitial.translations?.associateBy { it.langCode } ?: emptyMap()
                // 构建当前用户编辑后的映射
                val currentTransMap = translations.associateBy { it.langCode }
                // 最新数据中的翻译列表
                val latestTransList = latest.translations ?: emptyList()
                val latestTransMap = latestTransList.associateBy { it.langCode }

                val mergedTranslations = mutableListOf<SharedCategoryTranslation>()

                // 处理最新数据中存在的语言即别人未删除的语言
                for (latestTrans in latestTransList) {
                    val langCode = latestTrans.langCode
                    if (langCode in deletedLangCodes) {
                        continue
                    }
                    val current = currentTransMap[langCode]
                    val old = oldTransMap[langCode]
                    if (current != null && (old == null || current.name != old.name)) {
                        // 用户修改过，保留用户的值
                        mergedTranslations.add(current)
                    } else {
                        // 用户未修改，使用最新值
                        mergedTranslations.add(latestTrans)
                    }
                }

                // 处理用户新增的、最新数据中不存在的或已被别人删除的翻译
                for (current in translations) {
                    val langCode = current.langCode
                    // 上面已经处理过了，这里只处理最新数据中不存在的
                    if (langCode in latestTransMap) continue

                    val old = oldTransMap[langCode]
                    val isUserModified = (old == null || current.name != old.name)

                    if (isUserModified) {
                        // 用户修改过或新增，即使别人删除了，也保留用户的修改
                        mergedTranslations.add(current)
                    }
                }

                // 更新 UI 状态
                categoryName = mergedName
                categoryIva = mergedIva
                translations.clear()
                translations.addAll(mergedTranslations)

                // 更新快照和版本信息
                initialCategory = latest.copy(
                    name = mergedName,
                    iva = mergedIva,
                    translations = mergedTranslations
                )
                categoryVersion = latest.version
            }
        }
    }
}
