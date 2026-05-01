package org.dsqrwym.enterprise.ui.viewmodels.products

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.create_failed
import maian.shared.generated.resources.create_success
import org.dsqrwym.enterprise.data.category.CategoryRepository
import org.dsqrwym.enterprise.data.product.ProductRepository
import org.dsqrwym.enterprise.data.product.dto.ProductVariantDto
import org.dsqrwym.enterprise.navigation.Products
import org.dsqrwym.shared.data.file.SharedUploadRepository
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.serialization.OptionalField
import org.dsqrwym.shared.serialization.getValOrNull
import org.dsqrwym.shared.serialization.toOptionalField
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(FlowPreview::class, ExperimentalUuidApi::class)
class ProductCreateViewModel(
    uploadRepository: SharedUploadRepository,
    productRepository: ProductRepository,
    categoryRepository: CategoryRepository,
    snackbarViewModel: MySnackbarViewModel
) : BaseProductFormViewModel(
    uploadRepository,
    productRepository,
    categoryRepository,
    snackbarViewModel
) {
    var createFormUiState: UiState by mutableStateOf(UiState.Idle)
    val createButtonEnabled: Boolean by derivedStateOf {
        createFormUiState == UiState.Idle &&
                (mediaPicker.mediaPickerUiState == UiState.Success || mediaPicker.mediaPickerUiState == UiState.Idle)
                && productTranslationUiState == UiState.Loading
                && productMetaDataUiState == UiState.Loading
                && productVariantUiState == UiState.Loading
    }

    init {
        productVariants.add(
            ProductVariantDto(
                id = Uuid.generateV4().toString().toOptionalField(),
                typeSale = OptionalField.Value(SharedProductSaleVariant.BOX),
                price = "0.00".toOptionalField(),
                priceIva = "0.00".toOptionalField(),
                productCode = "SKU".toOptionalField(),
                availableStock = 100.toOptionalField(),
                saleUnitQty = getRecommendedSaleUnitQty(SharedProductSaleVariant.BOX).toOptionalField()
            )
        )
        productVariants.firstOrNull()?.id?.getValOrNull()?.let { firstId ->
            variantSaleUnitQtyManuallyEdited[firstId] = false
        }
    }

    fun createProduct() {
        if (!validateForm()) return
        if (!createButtonEnabled) return
        val primaryCategory = filterCategory ?: return
        createFormUiState = UiState.Loading
        val primaryTranslation = translationTabs.first().first.copy(
            description = translationTabs.first().second.toHtml()
        )
        val restTranslations = translationTabs.drop(1).map {
            it.first.copy(
                description = it.second.toHtml()
            )
        }
        productVariants.forEachIndexed { index, _ ->
            // create do not need id
            productVariants[index] =
                productVariants[index].copy(id = OptionalField.Undefined, sort = index.toOptionalField())
        }
        val files = buildFiles()
        viewModelScope.launch {
            when (val result = productRepository.createProduct(
                name = primaryTranslation.name,
                title = primaryTranslation.title,
                description = primaryTranslation.description,
                translations = restTranslations,
                iva = productIva,
                productCode = productCode,
                primaryCategoryId = primaryCategory.id,
                variants = productVariants,
                files = files
            )) {
                is SharedResponseResult.Success -> {
                    createFormUiState = UiState.Success
                    val message = getString(SharedRes.string.create_success)
                    mySnackbarViewModel.showSuccess(
                        message = message
                    )
                    emitNavigation(NavigationEvent.ToRoute(Products))
                }

                is SharedResponseResult.Error -> {
                    createFormUiState = UiState.Error
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    } else {
                        val message = getString(SharedRes.string.create_failed)
                        mySnackbarViewModel.showError(message)
                    }
                }
            }
            delay(500.milliseconds)
            createFormUiState = UiState.Idle
        }
    }
}
