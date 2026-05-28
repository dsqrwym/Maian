package org.dsqrwym.shared.ui.components.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.no_categories
import maian.shared.generated.resources.path
import org.dsqrwym.shared.paging.hasLoadError
import org.dsqrwym.shared.paging.isAppendingOrPrepending
import org.dsqrwym.shared.paging.isEmptyResult
import org.dsqrwym.shared.paging.isInitialLoading
import org.dsqrwym.shared.paging.isRefreshing
import org.dsqrwym.shared.ui.components.placeholder.SharedPlainNotFoundPlaceholder
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.colum.SharedLazyColumnLayout.appendErrorRetry
import org.dsqrwym.shared.util.colum.SharedLazyColumnLayout.appendLoadingIndicator
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.shared.util.row.SharedRowLayout
import org.jetbrains.compose.resources.stringResource

@Composable
fun SharedCategoryPathRow(
    pathNames: List<String>,
    currentName: String,
    modifier: Modifier = Modifier,
    onPathClick: (Int) -> Unit = {},
) {
    if (pathNames.size < 2) return
    FlowRow(
        modifier = modifier,
        horizontalArrangement = SharedRowLayout.arrangement,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "${stringResource(SharedRes.string.path)}: ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        pathNames.forEachIndexed { index, name ->
            if (index != 0) {
                Text(
                    "->",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                modifier = if (index < pathNames.lastIndex) {
                    Modifier.clickable { onPathClick(index) }
                } else {
                    Modifier
                },
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = if (name == currentName) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (name == currentName) FontWeight.SemiBold else FontWeight.Normal,
                softWrap = true,
            )
        }
    }
}

@Composable
fun <T : Any> SharedCategoryRail(
    categories: LazyPagingItems<T>,
    selectedId: String?,
    itemId: (T) -> String,
    itemName: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    drawerWidth: Dp = 128.dp,
    drawerContainerColor: Color = Color.Transparent,
) {
    val isLoading = categories.isRefreshing

    PermanentDrawerSheet(
        modifier = modifier.width(drawerWidth).fillMaxHeight(),
        drawerContainerColor = drawerContainerColor,
    ) {
        if (categories.isEmptyResult) {
            SharedPlainNotFoundPlaceholder(stringResource(SharedRes.string.no_categories))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = SharedColumnLayout.arrangement,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (categories.isInitialLoading) {
                    appendLoadingIndicator()
                } else if (categories.hasLoadError) {
                    appendErrorRetry { categories.retry() }
                } else {
                    items(
                        count = categories.itemCount,
                        key = categories.itemKey { itemId(it) },
                    ) { index ->
                        categories[index]?.let { category ->
                            NavigationDrawerItem(
                                modifier = Modifier.animateItem().placeholderWithShimmer(isLoading),
                                selected = selectedId == itemId(category),
                                onClick = { onSelect(category) },
                                label = {
                                    Text(
                                        itemName(category),
                                        maxLines = 3,
                                        softWrap = true,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                },
                            )
                        }
                    }
                }

                if (categories.isAppendingOrPrepending) {
                    appendLoadingIndicator()
                }
            }
        }
    }
}

@Composable
fun <T : Any> SharedChildCategoryGrid(
    categories: LazyPagingItems<T>,
    itemName: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    key: (index: Int) -> Any = { index -> index },
) {
    if (categories.isEmptyResult) return
    val isLoading = categories.isRefreshing

    LazyRow(
        modifier = modifier,
        horizontalArrangement = SharedRowLayout.arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (categories.isInitialLoading) {
            appendLoadingIndicator()
        } else if (categories.hasLoadError) {
            appendErrorRetry { categories.retry() }
        } else {
            items(
                count = categories.itemCount,
                key = key,
            ) { index ->
                categories[index]?.let { category ->
                    ElevatedAssistChip(
                        modifier = Modifier.animateItem().placeholderWithShimmer(isLoading),
                        onClick = { onSelect(category) },
                        label = {
                            Text(
                                itemName(category),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }
        }

        if (categories.isAppendingOrPrepending) {
            appendLoadingIndicator()
        }
    }
}
