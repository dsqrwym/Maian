package org.dsqrwym.shared.data.pagination

interface PaginationQuery {
    val page: Int
    val limit: Int
}