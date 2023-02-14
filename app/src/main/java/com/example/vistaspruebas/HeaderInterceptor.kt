package com.example.vistaspruebas

import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var token = ""
        val request = chain.request().newBuilder()
            .addHeader(
                "Accept", "application/json"
            )
            .addHeader("authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}