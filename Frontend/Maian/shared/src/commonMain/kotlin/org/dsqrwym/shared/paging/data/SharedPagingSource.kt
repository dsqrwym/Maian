package org.dsqrwym.shared.paging.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState

class SharedPagingSource<T : Any>(
    private val query: String?,
    private val pageSize: Int,
    private val fetchPage: suspend (page: Int, pageSize: Int, query: String?) -> List<T>
) :
    PagingSource<Int, T>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 1
        return try {
            // Keep backend page/limit pagination on one stable coordinate system.
            val items = fetchPage(page, pageSize, query)
            LoadResult.Page(
                data = items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (items.size < pageSize) null else page + 1
            )
        } catch (e: Throwable) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        } ?: 1
    }
}

fun <T : Any> createPager(
    query: String?,
    pageSize: Int = 20,
    enablePlaceholders: Boolean = false,
    fetchPage: suspend (page: Int, pageSize: Int, query: String?) -> List<T>
): Pager<Int, T> {
    return Pager(
        config = PagingConfig(
            pageSize = pageSize,
            initialLoadSize = pageSize,
            enablePlaceholders = enablePlaceholders
        ),
        pagingSourceFactory = {
            SharedPagingSource(query, pageSize, fetchPage)
        }
    )
}
