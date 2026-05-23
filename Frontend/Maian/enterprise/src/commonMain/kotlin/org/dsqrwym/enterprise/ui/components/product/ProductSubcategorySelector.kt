package org.dsqrwym.enterprise.ui.components.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.main_language
import maian.enterprise.generated.resources.product_auxiliary_categories
import maian.enterprise.generated.resources.product_auxiliary_categories_count
import maian.enterprise.generated.resources.select_auxiliary_product_category
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.delete
import maian.shared.generated.resources.field_optional
import maian.shared.generated.resources.search_categories
import org.dsqrwym.shared.domain.category.CategorySummary
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.util.clipboard.SharedClipboardData
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.modifier.copyOnInteraction
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.shared.util.row.SharedRowLayout
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSubcategorySelector(
    selectedCategories: List<CategorySummary>,
    canAddCategory: Boolean,
    maxCategories: Int,
    onAddCategory: (CategorySummary) -> Unit,
    onRemoveCategory: (String) -> Unit,
    onSearchCategory: suspend (String?, Int, Int) -> List<CategorySummary>,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    var selectedForSelector by remember { mutableStateOf<CategorySummary?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = SharedColumnLayout.arrangement,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().placeholderWithShimmer(isLoading),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(EnterpriseRes.string.product_auxiliary_categories),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = stringResource(
                    EnterpriseRes.string.product_auxiliary_categories_count,
                    selectedCategories.size,
                    maxCategories
                ),
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SearchableSelectorRemote(
            config = RemoteSearchableSelectorConfig(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                label = "${stringResource(EnterpriseRes.string.select_auxiliary_product_category)} (${stringResource(SharedRes.string.field_optional)})",
                error = null,
                enabled = canAddCategory && !isLoading,
                leadingIcon = Icons.Outlined.Category,
                placeholder = stringResource(SharedRes.string.search_categories),
                selectedItem = selectedForSelector,
                onSelectedItemChange = { category ->
                    category?.let(onAddCategory)
                    selectedForSelector = category
                },
                pageSize = 5,
                itemToString = {
                    "${it.name}${it.translationDisplayText()?.let { str -> " • $str" }.orEmpty()}"
                },
                onSearch = onSearchCategory,
            )
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = SharedRowLayout.arrangement,
            verticalArrangement = Arrangement.SpaceBetween,
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            selectedCategories.forEach { category ->
                ProductSubcategoryChip(
                    category = category,
                    enabled = !isLoading,
                    onRemove = { onRemoveCategory(category.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductSubcategoryChip(
    category: CategorySummary,
    enabled: Boolean,
    onRemove: () -> Unit,
) {
    val languageCode = LanguageManager.getCurrent().code
    val mainLanguageLabel = stringResource(EnterpriseRes.string.main_language)
    val tooltipText = remember(category, mainLanguageLabel) {
        category.productCategoryTooltipText(mainLanguageLabel)
    }
    val positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above)

    TooltipBox(
        state = rememberTooltipState(),
        positionProvider = positionProvider,
        tooltip = {
            PlainTooltip {
                SelectionContainer {
                    Text(
                        tooltipText,
                        modifier = Modifier.copyOnInteraction(SharedClipboardData.Text(tooltipText))
                    )
                }
            }
        }
    ) {
        InputChip(
            selected = true,
            enabled = enabled,
            onClick = onRemove,
            label = {
                Text(
                    text = category.productCategoryDisplayName(languageCode),
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = stringResource(SharedRes.string.delete),
                )
            },
        )
    }
}
