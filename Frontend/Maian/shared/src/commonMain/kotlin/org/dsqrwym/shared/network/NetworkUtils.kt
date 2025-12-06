package org.dsqrwym.shared.network

import io.ktor.http.*
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.session_not_found
import org.dsqrwym.shared.data.local.SharedUserPayloadStorage
import org.dsqrwym.shared.data.user.SharedUserPayload
import org.jetbrains.compose.resources.getString

suspend fun <T> withAuthOrError(
    block: suspend (user: SharedUserPayload) -> SharedResponseResult<T>
): SharedResponseResult<T> {

    val user = SharedUserPayloadStorage.get()
    if (user == null) {
        val message =
            getString(SharedRes.string.session_not_found)
        return SharedResponseResult.Error(HttpStatusCode.Unauthorized, message)
    }

    return block(user)
}