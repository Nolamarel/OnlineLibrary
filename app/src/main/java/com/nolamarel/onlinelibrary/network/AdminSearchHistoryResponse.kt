package com.nolamarel.onlinelibrary.network

data class AdminSearchHistoryResponse(
    val historyId: Long,
    val userId: Long,
    val query: String,
    val createdAt: String
)