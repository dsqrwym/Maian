package org.dsqrwym.shared.ui.viewmodels.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import org.dsqrwym.shared.data.pagination.createPager
import org.dsqrwym.shared.util.timing.SharedUiTiming

class SearchableSelectorRemoteViewModel<T : Any>(
    private val pageSize: Int,
    initialQuery: String? = null,
    private val onSearch: suspend (page: Int, pageSize: Int, query: String?) -> List<T>
) : ViewModel() {
    private val _query = MutableStateFlow(initialQuery)
    fun setQuery(query: String?) {
        _query.value = query
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pagingFlow = _query
        .debounce(SharedUiTiming.searchDebounce)
        .distinctUntilChanged()
        .flatMapLatest { q ->
            createPager(
                query = q,
                pageSize = pageSize,
                fetchPage = onSearch
            ).flow
        }
        .cachedIn(viewModelScope)
}
