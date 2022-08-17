package com.example.messenger.retrofit

data class RetrofitObject(
    val collapse_key: String,
    val `data`: Data,
    val notification: Notification,
    val to: String
)