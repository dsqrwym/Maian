package org.dsqrwym.shared.ui.components.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.path
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
    fallbackCategories: List<T>,
    selectedId: String?,
    itemId: (T) -> String,
    itemName: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    drawerWidth: Dp = 128.dp,
    drawerContainerColor: Color = Color.Transparent,
) {
    val isLoading =
        categories.loadState.append is LoadState.Loading || categories.loadState.refresh is LoadState.Loading
    val showFallbackDuringRefresh =
        categories.loadState.refresh is LoadState.Loading &&
                categories.itemCount == 0 &&
                fallbackCategories.isNotEmpty()

    PermanentDrawerSheet(
        modifier = modifier.width(drawerWidth).fillMaxHeight(),
        drawerContainerColor = drawerContainerColor,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = SharedColumnLayout.arrangement,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (showFallbackDuringRefresh) {
                items(
                    count = fallbackCategories.size,
                    key = { index -> itemId(fallbackCategories[index]) },
                ) { index ->
                    val category = fallbackCategories[index]
                    NavigationDrawerItem(
                        modifier = Modifier.placeholderWithShimmer(true),
                        selected = selectedId == itemId(category),
                        onClick = {},
                        label = {
                            Text(
                                itemName(category),
                                maxLines = 3,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            } else {
                items(
                    count = categories.itemCount,
                    key = { index -> categories.peek(index)?.let(itemId) ?: index },
                ) { index ->
                    categories[index]?.let { category ->
                        NavigationDrawerItem(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            selected = selectedId == itemId(category),
                            onClick = { onSelect(category) },
                            label = {
                                Text(
                                    itemName(category),
                                    maxLines = 3,
                                    softWrap = true,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }
            }
            if (categories.loadState.append is LoadState.Error) {
                item {
                    SharedRetryButton { categories.retry() }
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
    shouldHideWhenEmpty: Boolean = true,
) {
    if (shouldHideWhenEmpty && categories.itemCount == 0 && categories.loadState.refresh !is LoadState.Loading) return
    val isLoading =
        categories.loadState.append is LoadState.Loading || categories.loadState.refresh is LoadState.Loading

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(categories.itemCount) { index ->
            categories[index]?.let { category ->
                ElevatedAssistChip(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    onClick = { onSelect(category) },
                    label = {
                        Text(
                            itemName(category),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }

        if (categories.loadState.append is LoadState.Error) {
            SharedRetryButton { categories.retry() }
        }
    }
}
