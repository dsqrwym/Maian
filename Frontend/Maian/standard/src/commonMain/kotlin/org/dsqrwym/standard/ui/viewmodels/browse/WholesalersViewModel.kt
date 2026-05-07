package org.dsqrwym.standard.ui.viewmodels.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.standard.data.browse.RetailBrowseRepository
import org.dsqrwym.standard.domain.browse.RetailWholesaler

class WholesalersViewModel(
    private val repository: RetailBrowseRepository,
) : ViewModel() {
    var searchText by mutableStateOf("")
        private set
    var wholesalers by mutableStateOf<List<RetailWholesaler>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var submittedSearch = ""
    private var loaded = false

    fun ensureLoaded() {
        if (loaded) return
        loaded = true
        loadWholesalers()
    }

    fun updateSearchText(value: String) {
        searchText = value
    }

    fun submitSearch() {
        submittedSearch = searchText
        loadWholesalers()
    }

    fun clearSearch() {
        searchText = ""
        submittedSearch = ""
        loadWholesalers()
    }

    private fun loadWholesalers() {
        viewModelScope.launch {
            isLoading = true
            error = null
            when (val result = repository.getWholesalers(search = submittedSearch)) {
                is SharedResponseResult.Success -> wholesalers = result.data?.items.orEmpty()
                is SharedResponseResult.Error -> {
                    wholesalers = emptyList()
                    error = result.message
                }
            }
            isLoading = false
        }
    }
}
