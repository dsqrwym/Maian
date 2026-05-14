package org.dsqrwym.standard.di

import org.dsqrwym.shared.di.sharedInitKoin
import org.dsqrwym.standard.di.auth.standardAuthModule
import org.dsqrwym.standard.di.browse.standardBrowseModule
import org.dsqrwym.standard.di.profile.standardProfileModule

fun standardInitKoin(){
    sharedInitKoin{
        modules(standardModule)
        modules(standardAuthModule)
        modules(standardBrowseModule)
        modules(standardProfileModule)
    }
}
