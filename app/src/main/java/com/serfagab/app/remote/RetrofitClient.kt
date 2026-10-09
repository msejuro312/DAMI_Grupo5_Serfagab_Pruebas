package com.serfagab.app.remote

import com.serfagab.app.util.SessionStore
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val clienteOkHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { cadena ->
                val original = cadena.request()
                val solicitud = obtenerSolicitudConAuth(original)
                cadena.proceed(solicitud)
            }
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clienteOkHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private fun obtenerSolicitudConAuth(original: Request): Request {
        val cabecera = SessionStore.obtenerCabeceraBasicAuth()
        return if (cabecera != null) {
            original.newBuilder().header("Authorization", cabecera).build()
        } else {
            original
        }
    }
}