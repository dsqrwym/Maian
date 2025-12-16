package org.dsqrwym.enterprise.ui.viewmodels.products

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import io.github.vinceglb.filekit.PlatformFile


class ProductCreateViewModel : ViewModel() {
    private val _localUploaderFile = mutableStateListOf<PlatformFile>()
    val localUploaderFile: List<PlatformFile> = _localUploaderFile

    fun addLocalFile(file: PlatformFile) {
        _localUploaderFile.add(file)
    }

    fun removeLocalFile(file: PlatformFile) {
        _localUploaderFile.remove(file)
    }
}