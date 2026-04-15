package com.nolamarel.onlinelibrary.network

data class SearchHistoryDto(
    val historyId: Long,
    val query: String,
    val createdAt: String
)