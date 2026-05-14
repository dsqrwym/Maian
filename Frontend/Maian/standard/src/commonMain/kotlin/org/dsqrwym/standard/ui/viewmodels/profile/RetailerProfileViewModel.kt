package org.dsqrwym.standard.ui.viewmodels.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.load_failed
import org.dsqrwym.shared.data.profile.RetailerProfileResponseDto
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.standard.data.profile.RetailerProfileRepository
import org.jetbrains.compose.resources.getString

class RetailerProfileViewModel(
    private val repository: RetailerProfileRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
) : ViewModel() {
    var profile by mutableStateOf<RetailerProfileResponseDto?>(null)
        private set

    var uiState by mutableStateOf(UiState.Idle)
        private set

    val isLoading: Boolean
        get() = uiState == UiState.Loading

    fun loadProfile() {
        viewModelScope.launch {
            uiState = UiState.Loading
            when (val result = repository.getMyProfile()) {
                is SharedResponseResult.Success -> {
                    profile = result.data
                    uiState = UiState.Success
                }

                is SharedResponseResult.Error -> {
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    } else {
                        mySnackbarViewModel.showError(
                            result.message ?: getString(SharedRes.string.load_failed)
                        )
                    }
                    uiState = UiState.Error
                }
            }
        }
    }

    fun refreshProfile() {
        loadProfile()
    }
}
