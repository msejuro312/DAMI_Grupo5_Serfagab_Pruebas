package com.serfagab.app.remote

import com.serfagab.app.model.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<Usuario>
}