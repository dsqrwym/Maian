@file:OptIn(ExperimentalMaterial3Api::class)

package org.dsqrwym.shared.ui.components.input.selector

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.address_no_match
import org.dsqrwym.shared.ui.components.input.outlinetextfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.progressindicators.MyCircularProgressIndicator
import org.jetbrains.compose.resources.stringResource
import kotlin.math.min

data class RemoteSearchableSelectorConfig<T>(
    val modifier: Modifier = Modifier,
    val label: String = "",
    val items: List<T> = listOf(),
    val selectedItem: T? = null,
    val onSelectedItemChange: (T?) -> Unit = {},
    val placeholder: String = "",
    val error: String? = null,
    val enabled: Boolean = true,
    val leadingIcon: ImageVector = Icons.Outlined.Search,
    val onSearch: suspend (query: String?, page: Int, limit: Int) -> Unit, // 异步请求
    val pageSize: Int = 20,
    val itemToString: (T) -> String,
    val itemId: (T) -> String = { itemToString(it) },
    val semanticsPropertyReceiver: SemanticsPropertyReceiver.() -> Unit = {},
    val imeAction: ImeAction = ImeAction.Done,
    val onImeAction: () -> Unit = {}
)


data class SearchableSelectorDefaults(
    val modifier: Modifier = Modifier,
    val label: String = "",
    val selectedItemId: String? = null,
    val onSelectedItemIdChange: ((String?) -> Unit)? = null,
    val placeholder: String = "",
    val error: String? = null,
    val enabled: Boolean = true,
    val leadingIcon: ImageVector = Icons.Outlined.Search,
    val isSearching: Boolean? = null,
    val onSearchingChange: ((Boolean) -> Unit)? = null,
    val semanticsPropertyReceiver: SemanticsPropertyReceiver.() -> Unit = {},
    val imeAction: ImeAction = ImeAction.Done,
    val onImeAction: () -> Unit = {},
)

@Composable
fun <T> SearchableSelector(
    items: List<T>,
    itemToString: (T) -> String,
    itemId: (T) -> String = { itemToString(it) },
    config: SearchableSelectorDefaults = SearchableSelectorDefaults()
) {
    val isControlled = config.onSelectedItemIdChange != null

    var internalSelectedItem by remember { mutableStateOf<T?>(null) }
    var text by remember { mutableStateOf("") }
    var filteredList by remember { mutableStateOf(items) }
    var expanded by remember { mutableStateOf(false) }
    var suppressOnDismiss by remember { mutableStateOf(false) }
    var explicitSelection by remember { mutableStateOf(false) }

    var internalSearching by remember { mutableStateOf(false) }
    val searching = config.isSearching ?: internalSearching

    val coroutineScope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val selectedItem: T? = if (isControlled) {
        items.firstOrNull { config.selectedItemId != null && itemId(it) == config.selectedItemId }
    } else {
        internalSelectedItem
    }

    LaunchedEffect(items, config.selectedItemId, internalSelectedItem) {
        text = selectedItem?.let { itemToString(it) } ?: ""
        filteredList = items
    }

    fun filtered(query: String): List<T> {
        if (query.isBlank()) return items
        val q = query.trim().lowercase()
        val contains = items.filter { itemToString(it).lowercase().contains(q) }
        if (contains.isNotEmpty()) return contains
        val starts = items.filter { itemToString(it).lowercase().startsWith(q) }
        if (starts.isNotEmpty()) return starts
        return items.sortedBy { levenshtein(it = itemToString(it).lowercase(), s = q) }.take(10)
    }

    fun bestMatchFor(query: String): T? {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return null
        val byContains = items.filter { itemToString(it).lowercase().contains(q) }
        if (byContains.isNotEmpty()) return byContains.first()
        val byPrefix = items.filter { itemToString(it).lowercase().startsWith(q) }
        if (byPrefix.isNotEmpty()) return byPrefix.first()
        return items.minByOrNull { levenshtein(it = itemToString(it).lowercase(), s = q) }
    }

    Column(modifier = config.modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {/*handled manually in trailing icon */ }
        ) {
            MyOutlinedTextField(
                leadingIcon = config.leadingIcon,
                enabled = config.enabled,
                value = text,
                onValueChange = {
                    text = it
                    explicitSelection = false

                    searchJob?.cancel()
                    searchJob = coroutineScope.launch {
                        if (config.isSearching == null) internalSearching = true
                        config.onSearchingChange?.invoke(true)

                        delay(500)
                        filteredList = filtered(it)
                        expanded = true

                        if (config.isSearching == null) internalSearching = false
                        config.onSearchingChange?.invoke(false)
                    }
                },
                error = config.error,
                labelText = config.label,
                placeholderText = config.placeholder,
                trailingIcon = {
                    if (searching) {
                        MyCircularProgressIndicator(
                            size = 18.dp,
                            progressStrokeWith = 2.dp
                        )
                    } else {
                        IconButton(
                            enabled = config.enabled,
                            onClick = {
                                suppressOnDismiss = true
                                expanded = !expanded
                                if (expanded) {
                                    filteredList = items
                                }
                            }) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    }
                },
                semanticsPropertyReceiver = config.semanticsPropertyReceiver,
                imeAction = config.imeAction,
                onImeAction = {
                    if (!explicitSelection) {
                        val best = bestMatchFor(text)
                        if (isControlled) {
                            config.onSelectedItemIdChange.invoke(best?.let { itemId(it) })
                        } else {
                            internalSelectedItem = best
                        }
                        best?.let { text = itemToString(it) }
                        config.onSelectedItemIdChange?.invoke(best?.let { itemId(it) })
                    }
                    if (expanded) {
                        expanded = false
                    }
                    config.onImeAction()
                },
                keyBordType = KeyboardType.Text
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    if (suppressOnDismiss) {
                        // 忽略由按钮点击产生的那次 onDismiss
                        suppressOnDismiss = false
                        return@ExposedDropdownMenu
                    }
                    expanded = false
                }
            ) {
                if (filteredList.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text(stringResource(SharedRes.string.address_no_match)) },
                        onClick = { /* no-op */ })
                } else {
                    filteredList.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(itemToString(item)) },
                            onClick = {
                                explicitSelection = true
                                text = itemToString(item)
                                expanded = false
                                if (isControlled) {
                                    config.onSelectedItemIdChange.invoke(itemId(item))
                                } else {
                                    internalSelectedItem = item
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun levenshtein(it: String, s: String): Int {
    if (it == s) return 0
    if (it.isEmpty()) return s.length
    if (s.isEmpty()) return it.length

    val v0 = IntArray(s.length + 1) { it }
    val v1 = IntArray(s.length + 1)

    for (i in it.indices) {
        v1[0] = i + 1
        for (j in s.indices) {
            val cost = if (it[i] == s[j]) 0 else 1
            v1[j + 1] = min(min(v1[j] + 1, v0[j + 1] + 1), v0[j] + cost)
        }
        for (j in v0.indices) v0[j] = v1[j]
    }
    return v1[s.length]
}


@OptIn(FlowPreview::class)
@Composable
fun <T> SearchableSelectorRemote(
    config: RemoteSearchableSelectorConfig<T>
) {
    val selectedItem = config.selectedItem
    var query by remember { mutableStateOf(selectedItem?.let { config.itemToString(it) } ?:"") }
    var expanded by remember { mutableStateOf(false) }
    var suppressOnDismiss by remember { mutableStateOf(false) }
    var suppressNextSearch by remember { mutableStateOf(false) }
    val items = config.items
    var page by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }
    var endReached by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun fetchPage(query: String?, requestPage: Int) {
        if (endReached && requestPage > page) return
        if (requestPage == 1) coroutineScope.coroutineContext.cancelChildren()
        coroutineScope.launch {
            isLoading = true
            try {
                config.onSearch(query, requestPage, config.pageSize)
                page = requestPage
                endReached = items.size < config.pageSize // 一页未满就是末页
                if (!query.isNullOrBlank()) {
                    expanded = true
                }
            } catch (e: Exception) {
                throw e
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { query }
            .debounce(600)
            .distinctUntilChanged()
            .filter { !suppressNextSearch }
            .collectLatest { last ->
                page = 1
                endReached = false
                fetchPage(last, page)
            }
    }

    LaunchedEffect(selectedItem) {
        query = selectedItem?.let { config.itemToString(it) } ?: ""
    }

    Column(modifier = config.modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {/*handled manually in trailing icon */ }) {
            MyOutlinedTextField(
                leadingIcon = config.leadingIcon,
                enabled = config.enabled,
                value = query,
                onValueChange = { query = it },
                error = config.error,
                labelText = config.label,
                placeholderText = config.placeholder,
                trailingIcon = {
                    if (isLoading) {
                        MyCircularProgressIndicator(size = 18.dp, progressStrokeWith = 2.dp)
                    } else {
                        IconButton(
                            enabled = config.enabled,
                            onClick = {
                                suppressOnDismiss = true
                                expanded = !expanded
                            }
                        ) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    }
                },
                semanticsPropertyReceiver = config.semanticsPropertyReceiver,
                imeAction = config.imeAction,
                onImeAction = config.onImeAction
            )


            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    if (suppressOnDismiss) {
                        // 忽略由按钮点击产生的那次 onDismiss
                        suppressOnDismiss = false
                        return@ExposedDropdownMenu
                    }
                    expanded = false
                }
            ) {
                if (items.isEmpty() && !isLoading) {
                    DropdownMenuItem(
                        text = { Text(stringResource(SharedRes.string.address_no_match)) },
                        onClick = { /* no-op */ })
                } else {
                    items.forEachIndexed { index, item ->
                        DropdownMenuItem(
                            text = { Text(config.itemToString(item)) },
                            onClick = {
                                suppressNextSearch = true
                                config.onSelectedItemChange(item)
                                query = config.itemToString(item)
                                expanded = false
                            }
                        )

                        // 当快到列表底部时加载下一页
                        if (!endReached && index >= items.lastIndex - 3 && !isLoading) {
                            val nextPage = page + 1
                            fetchPage(
                                if (query == selectedItem?.let { config.itemToString(it) }) "" else query,
                                nextPage
                            )
                        }
                    }
                }
            }
        }
    }
}
