package org.dsqrwym.enterprise.data.auth

import io.ktor.client.*
import org.dsqrwym.shared.data.auth.SharedAuthApi

/**
 * Enterprise认证API
 * 直接使用shared模块的API，并添加Enterprise特有的注册功能
 */
class EnterpriseAuthApi(
    private val sharedAuthApi: SharedAuthApi,
    private val client: HttpClient
) {
    

}
