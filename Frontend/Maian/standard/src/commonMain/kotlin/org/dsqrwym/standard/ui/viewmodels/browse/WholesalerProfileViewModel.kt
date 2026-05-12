package org.dsqrwym.standard.ui.viewmodels.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.profile.SharedWholesalerProfileRepository
import org.dsqrwym.shared.data.profile.WholesalerProfileResponseDto
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.jetbrains.compose.resources.getString

class WholesalerProfileViewModel(
    private val repository: SharedWholesalerProfileRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
) : ViewModel() {
    var profile by mutableStateOf<WholesalerProfileResponseDto?>(null)
        private set

    var uiState by mutableStateOf(UiState.Idle)
        private set

    val isLoading: Boolean
        get() = uiState == UiState.Loading

    fun loadProfile(id: String) {
        viewModelScope.launch {
            uiState = UiState.Loading
            when (val result = repository.getWholesalerProfile(id)) {
                is SharedResponseResult.Success -> {
                    profile = result.data
                    uiState = UiState.Success
                }

                is SharedResponseResult.Error -> {
                    mySnackbarViewModel.showError(
                        result.message ?: getString(SharedRes.string.load_failed),
                    )
                    uiState = UiState.Error
                }
            }
        }
    }
}
