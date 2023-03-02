package com.example.vistaspruebas

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var token = ""
        token = userToken
        println("$userToken EL TOKEN")
        val request = chain.request().newBuilder()
            .addHeader(
                //"Accept", "application/json"
                "Content-Type", "application/json"
            )
            .addHeader("authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}