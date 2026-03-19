package org.dsqrwym.enterprise.ui.viewmodels.products

import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.dsqrwym.business.ui.media.MediaPickerViewModel
import org.dsqrwym.enterprise.data.category.CategoryRepository
import org.dsqrwym.enterprise.data.product.ProductRepository
import org.dsqrwym.enterprise.ui.viewmodels.categories.BaseCategoryFilterViewmodel
import org.dsqrwym.shared.data.category.dto.ReducedCategoryResponse
import org.dsqrwym.shared.data.file.SharedUploadRepository
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.localization.customAppLocale
import org.dsqrwym.shared.network.ErrorMessageMapper
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.formatter.toFixed
import org.dsqrwym.shared.util.validation.sanitizeIvaInput
import org.dsqrwym.shared.util.validation.sanitizeProductCode

@OptIn(FlowPreview::class)
class ProductCreateViewModel(
    private val uploadRepository: SharedUploadRepository,
    private val productRepository: ProductRepository,
    categoryRepository: CategoryRepository,
    private val snackbarViewModel: MySnackbarViewModel
) : BaseCategoryFilterViewmodel(
    categoryRepository,
    snackbarViewModel
) {
    // 产品媒体
    val mediaPicker = MediaPickerViewModel(
        uploadRepository = uploadRepository,
        coroutineScope = viewModelScope,
        snackbarViewModel = snackbarViewModel
    )

    // 产品信息 (多语言)
    var selectedLanguageIndex by mutableIntStateOf(0)
        private set
    var translationTabs = mutableStateListOf(
        Pair(
            SharedProductTranslation(
                LanguageManager.getCurrent().code,
                "",
                ""
            ),
            RichTextState()
        )
    )
        private set
    var showAddLanguageDialog by mutableStateOf(false)
        private set

    var productIva by mutableStateOf("")
        private set

    // 用户输入则使用用户输入否则根据选择类别
    var isIvaManuallyEdited by mutableStateOf(false)
        private set

    var productCode by mutableStateOf("")
        private set

    var productStatus by mutableStateOf(SharedProductStatus.ACTIVE)
        private set

    init {
        // 监听语言
        viewModelScope.launch {
            snapshotFlow { customAppLocale }.collectLatest { _ ->
                val currentCode = LanguageManager.getCurrent().code
                val existingIndex = translationTabs.indexOfFirst { it.first.langCode == currentCode }

                if (existingIndex == -1) {
                    // 如果不存在，添加并放在第一位
                    translationTabs.add(
                        0,
                        Pair(
                            SharedProductTranslation(currentCode, "", ""),
                            RichTextState()
                        )
                    )
                } else if (existingIndex != 0) {
                    // 如果存在但不在第一位，移动到第一位
                    val existing = translationTabs.removeAt(existingIndex)
                    translationTabs.add(0, existing)
                }
                // 自动选中当前语言（第一位）
                selectedLanguageIndex = 0
            }
        }
    }

    fun showAddLanguageDialog(value: Boolean) {
        showAddLanguageDialog = value
    }

    fun changeLanguageIndex(languageIndex: Int) {
        selectedLanguageIndex = languageIndex
    }

    fun upsertTranslation(langCode: String, name: String, title: String?, description: String? = null) {
        translationTabs.indexOfFirst { it.first.langCode == langCode }.let {
            if (it != -1) translationTabs[it] =
                translationTabs[it].copy(
                    first = SharedProductTranslation(
                        langCode,
                        name = name.take(50),
                        title = title?.take(50),
                        description = description
                    )
                )
            else translationTabs.add(
                Pair(
                    SharedProductTranslation(
                        langCode = langCode,
                        name = name.take(50),
                        title = title?.take(100),
                        description = description,
                    ),
                    RichTextState()
                )
            )
        }
    }

    fun removeTranslation(langCode: String) {
        translationTabs.find { it.first.langCode == langCode }?.let {
            translationTabs.remove(it)
        }
    }

    fun getAvailableLanguages(): List<LanguageManager.SupportedLanguages> {
        return LanguageManager.SupportedLanguages.entries
            .filter { it.code !in translationTabs.map { translation -> translation.first.langCode } }
    }

    fun updateProductCode(code: String) {
        productCode = sanitizeProductCode(code)
    }

    fun updateProductIva(iva: String) {
        val result = sanitizeIvaInput(iva) ?: return
        productIva = result
        isIvaManuallyEdited = result.isNotBlank()
    }

    fun formatIvaTwoDecimal() {
        if (productIva.isBlank()) return
        productIva = productIva.toDoubleOrNull()?.toFixed(2) ?: "0.00"
    }

    fun updateProductStatus(state: SharedProductStatus) {
        productStatus = state
    }

    override suspend fun findCategories(query: String?, page: Int, limit: Int): List<ReducedCategoryResponse> {
        when (val result = categoryRepository.getCategoriesByLevel(query, page, limit, true)) {
            is SharedResponseResult.Success -> {
                return result.data?.items ?: emptyList()
            }

            is SharedResponseResult.Error -> {
                if (ErrorMessageMapper.shouldShowToUser(result.type)) {
                    result.message?.let { mySnackbarViewModel.showError(it) }
                }
            }
        }
        return emptyList()
    }

    override fun updateFilterCategory(category: ReducedCategoryResponse?) {
        super.updateFilterCategory(category)
        val iva = category?.iva ?: return

        // 没有被用户改过自动填充
        if (!isIvaManuallyEdited) {
            productIva = iva
        }
    }
}