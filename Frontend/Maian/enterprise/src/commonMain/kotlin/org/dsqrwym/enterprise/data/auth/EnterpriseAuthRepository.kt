package org.dsqrwym.enterprise.data.auth

import org.dsqrwym.shared.data.auth.SharedAuthRepository

/**
 * Enterprise认证仓库
 * 直接使用shared模块的Repository，并添加Enterprise特有的注册功能
 */
class EnterpriseAuthRepository(
    private val sharedAuthRepository: SharedAuthRepository,
    private val api: EnterpriseAuthApi
) {

}
