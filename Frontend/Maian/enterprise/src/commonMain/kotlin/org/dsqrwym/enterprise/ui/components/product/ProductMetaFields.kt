package org.dsqrwym.enterprise.ui.components.product

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.parent_category_selected
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.enter_product_code
import maian.enterprise.generated.resources.select_main_product_category
import maian.shared.generated.resources.*
import org.dsqrwym.business.drawable.sharedicons.Barcode
import org.dsqrwym.business.ui.components.category.BusinessSelectedInfoCard
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.toStringResource
import org.dsqrwym.shared.domain.category.CategorySummary
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.InProgress
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.buttons.SharedScannerButton
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedDoubleField
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.input.selector.Selector
import org.dsqrwym.shared.ui.components.input.selector.SelectorConfig
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.shared.util.row.SharedRowLayout
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun ProductMetaFields(
    isLoading: Boolean = false,
    selectedCategory: CategorySummary?,
    onSelectedCategoryChange: (CategorySummary?) -> Unit,
    onSearchCategory: suspend (String?, Int, Int) -> List<CategorySummary>,
    onRemoveCategory: () -> Unit,
    categoryError: StringResource?,
    productCode: String = "",
    productCodeError: StringResource? = null,
    onProductCodeChange: (String) -> Unit = {},
    productIva: String = "",
    onIvaChange: (String?) -> Unit = {},
    productStatus: SharedProductStatus = SharedProductStatus.ACTIVE,
    onProductStatusChange: (SharedProductStatus) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val productStatusLabels = SharedProductStatus.entries.associateWith { stringResource(it.toStringResource()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = SharedColumnLayout.arrangement,
    ) {
        SearchableSelectorRemote(
            config = RemoteSearchableSelectorConfig(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                label = "${stringResource(EnterpriseRes.string.select_main_product_category)} (${stringResource(SharedRes.string.field_required)})",
                error = categoryError.asString(),
                leadingIcon = Icons.Outlined.Category,
                selectedItem = selectedCategory,
                onSelectedItemChange = onSelectedCategoryChange,
                pageSize = 100,
                itemToString = {
                    "${it.name}${it.translationDisplayText()?.let { str -> " • $str" }.orEmpty()}"
                },
                onSearch = onSearchCategory,
            )
        )

        BusinessSelectedInfoCard(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            visible = selectedCategory != null,
            title = stringResource(BusinessRes.string.parent_category_selected),
            description = selectedCategory?.name ?: "",
            onClear = onRemoveCategory,
            enabled = true
        )


        MyOutlinedTextField(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            value = productCode,
            onValueChange = onProductCodeChange,
            leadingIcon = SharedIcons.Barcode,
            leadingIconContentDescription = stringResource(SharedRes.string.product_code),
            trailingIcon = {
                if (productCode.isBlank()) {
                    SharedScannerButton(onProductCodeChange)
                } else SharedCloseButton { onProductCodeChange("") }
            },
            labelText = "${stringResource(SharedRes.string.product_code)} (${stringResource(SharedRes.string.field_required)})",
            placeholderText = stringResource(EnterpriseRes.string.enter_product_code),
            error = productCodeError.asString(),
            keyBordType = KeyboardType.Uri,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )

        Row(
            horizontalArrangement = SharedRowLayout.arrangement,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MyOutlinedDoubleField(
                value = productIva,
                modifier = Modifier.weight(0.5f).placeholderWithShimmer(isLoading),
                modifierFillMaxWidth = false,
                onValueChange = {
                    onIvaChange(it)
                },
                leadingIcon = Icons.Outlined.Percent,
                leadingIconContentDescription = stringResource(SharedRes.string.tax_rate),
                labelText = "${stringResource(SharedRes.string.tax_rate)}->IVA(%) (${stringResource(SharedRes.string.field_required)})",
                error = null,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Next) }
            )

            Selector(
                items = SharedProductStatus.entries,
                itemToString = { productStatusLabels[it].orEmpty() },
                selectedItem = productStatus,
                onItemSelected = {
                    it?.let { onProductStatusChange(it) }
                },
                config = SelectorConfig(
                    modifier = Modifier.weight(0.5f).placeholderWithShimmer(isLoading),
                    label = "${stringResource(SharedRes.string.status)} (${stringResource(SharedRes.string.field_required)})",
                    leadingIcon = SharedIcons.InProgress,
                    modifierFillMaxWidth = false,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
        }
    }
}



