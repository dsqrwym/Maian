package org.dsqrwym.standard.ui.viewmodels.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.standard.data.browse.RetailBrowseRepository
import org.dsqrwym.standard.domain.browse.RetailDistributor

class DistributorsViewModel(
    private val repository: RetailBrowseRepository,
) : ViewModel() {
    var searchText by mutableStateOf("")
        private set
    var distributors by mutableStateOf<List<RetailDistributor>>(emptyList())
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
        loadDistributors()
    }

    fun updateSearchText(value: String) {
        searchText = value
    }

    fun submitSearch() {
        submittedSearch = searchText
        loadDistributors()
    }

    fun clearSearch() {
        searchText = ""
        submittedSearch = ""
        loadDistributors()
    }

    private fun loadDistributors() {
        viewModelScope.launch {
            isLoading = true
            error = null
            when (val result = repository.getDistributors(search = submittedSearch)) {
                is SharedResponseResult.Success -> distributors = result.data?.items.orEmpty()
                is SharedResponseResult.Error -> {
                    distributors = emptyList()
                    error = result.message
                }
            }
            isLoading = false
        }
    }
}
