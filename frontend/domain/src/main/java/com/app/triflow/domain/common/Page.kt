package com.app.triflow.domain.common

data class Page(val limit: Int = 20, val offset: Int = 0) {
    init {
        require(limit in 1..100) { "limit must be in 1..100" }
        require(offset >= 0) { "offset must be >= 0" }
    }
}

data class Paged<T>(
    val items: List<T>,
    val limit: Int,
    val offset: Int,
    val total: Int,
)
