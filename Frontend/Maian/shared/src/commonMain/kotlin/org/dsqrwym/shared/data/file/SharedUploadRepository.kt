package org.dsqrwym.shared.data.file

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.reset_unknown_error
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.network.withAuthOrError
import org.jetbrains.compose.resources.getString

class SharedUploadRepository(
    private val api: SharedUploadApi
) {
    // 互斥锁
    private val mutex = Mutex()
    private val jobs = mutableMapOf<String, Job>()
    private val flows = mutableMapOf<String, SharedFlow<SharedUploadEvent>>()

    suspend fun uploadFile(
        localId: String,
        file: PlatformFile,
        scope: CoroutineScope
    ): SharedFlow<SharedUploadEvent> {
        return mutex.withLock {
            // 如果已经有流在运行，直接返回现有的
            flows[localId]?.let { return@withLock it }

            // 创建一个热流（手动实现 shareIn 的效果）
            // replay = 1 确保订阅者进来就能看到最后的状态
            val sharedFlow = MutableSharedFlow<SharedUploadEvent>(
                replay = 1,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            )

            // scope.launch 会立即返回一个 Job 实例
            val job = scope.launch(Dispatchers.Default) {
                try {
                    val result =
                        withAuthOrError { user ->
                            safeApiCall {
                                api.uploadFile(file, user.wholesalerId ?: user.userId) { sent, total ->
                                    if (total > 0) {
                                        sharedFlow.tryEmit(SharedUploadEvent.Progress(sent.toFloat() / total))
                                    }
                                }
                            }
                        }
                    when (result) {
                        is SharedResponseResult.Success -> {
                            if (result.data == null) {
                                sharedFlow.tryEmit(SharedUploadEvent.Error(getString(SharedRes.string.reset_unknown_error)))
                            } else {
                                sharedFlow.tryEmit(SharedUploadEvent.Success(result.data.id))
                            }
                        }

                        is SharedResponseResult.Error -> {
                            val message =
                                if (SharedResponseResult.shouldShowToUser(result.type)) result.message
                                else getString(SharedRes.string.reset_unknown_error)
                            sharedFlow.tryEmit(SharedUploadEvent.Error(message))
                        }
                    }
                } catch (e: Exception) {
                    sharedFlow.tryEmit(SharedUploadEvent.Error(e.message))
                } finally {
                    // 无论成功失败取消，都从 map 中移除自己
                    cleanup(localId)
                }
            }

            // 在锁释放之前，完成 Job 和 Flow 的登记 也就是说就算另一个线程立刻调用 cancel()，它也必须等这个锁释放。而锁释放时，jobs[localId] 已经有值了，所以一定能取消掉
            jobs[localId] = job
            flows[localId] = sharedFlow

            sharedFlow
        }
    }

    private suspend fun cleanup(localId: String) {
        mutex.withLock {
            jobs.remove(localId)
            flows.remove(localId)
        }
    }

    fun cancel(localId: String, scope: CoroutineScope) {
        scope.launch {
            mutex.withLock {
                jobs.remove(localId)?.cancel()
                flows.remove(localId)
            }
        }
    }
}