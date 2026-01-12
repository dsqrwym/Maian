package org.dsqrwym.enterprise.ui.viewmodels.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.dsqrwym.business.ui.media.MediaPickerViewModel
import org.dsqrwym.shared.data.file.SharedUploadRepository
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel

class ProductCreateViewModel(
    private val uploadRepository: SharedUploadRepository,
    private val snackbarViewModel: MySnackbarViewModel
) : ViewModel() {
    val mediaPicker = MediaPickerViewModel(
        uploadRepository = uploadRepository,
        coroutineScope = viewModelScope,
        snackbarViewModel = snackbarViewModel
    )

}