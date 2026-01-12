package org.dsqrwym.shared.di.file

import org.dsqrwym.shared.data.file.SharedUploadApi
import org.dsqrwym.shared.data.file.SharedUploadRepository
import org.koin.dsl.module

val sharedFileModule = module {
    single { SharedUploadApi(get()) }
    single { SharedUploadRepository(get()) }
}