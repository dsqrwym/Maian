package org.dsqrwym.admin.data.user

import org.dsqrwym.admin.data.user.dto.WholeSalerUserResponse
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.user.SharedUserApi
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.data.user.UserStatus
import org.dsqrwym.shared.data.user.dto.FindUserQueryDto
import org.dsqrwym.shared.data.user.dto.FindUserQueryOrderBy
import org.dsqrwym.shared.network.ApiResponseList
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.network.toSharedResponseResult

class UserRepository(val api: SharedUserApi) {
    suspend fun getWholesalers(
        query: String? = null,
        page: Int = 1,
        limit: Int = 100
    ): SharedResponseResult<ApiResponseList<WholeSalerUserResponse>> {
        val query = FindUserQueryDto(
            search = query,
            role = UserRole.WHOLESALER,
            status = UserStatus.APPROVED,
            page = page,
            limit = limit,
            orderBy = FindUserQueryOrderBy.USER_ID,
            orderDir = OrderDir.DESC,
            userId = true,
            username = true,
        )
        return api.getUsers<WholeSalerUserResponse>(query).toSharedResponseResult()
    }
}