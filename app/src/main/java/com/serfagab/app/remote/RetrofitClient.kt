package com.serfagab.app.remote

import android.util.Log
import com.serfagab.app.BuildConfig
import com.serfagab.app.util.SessionStore
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val ETIQUETA_LOG = "SerfagabHttp"
    private const val RUTA_LOGIN = "/api/auth/login"

    private val clienteOkHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { cadena ->
                val original = cadena.request()
                val solicitud = obtenerSolicitudConAuth(original)
                registrarSolicitud(solicitud)
                try {
                    val respuesta = cadena.proceed(solicitud)
                    registrarRespuesta(respuesta)
                    respuesta
                } catch (e: IOException) {
                    registrarErrorConexion(solicitud, e)
                    throw e
                }
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
        if (esRutaPublica(original)) {
            return original
        }
        val cabecera = SessionStore.obtenerCabeceraBasicAuth() ?: return original
        return original.newBuilder().header("Authorization", cabecera).build()
    }

    private fun esRutaPublica(request: Request): Boolean {
        return request.url().encodedPath().endsWith(RUTA_LOGIN)
    }

    private fun registrarSolicitud(request: Request) {
        if (BuildConfig.DEBUG) {
            Log.d(ETIQUETA_LOG, "Solicitud ${request.method()} ${request.url().encodedPath()}")
        }
    }

    private fun registrarRespuesta(response: Response) {
        if (BuildConfig.DEBUG) {
            Log.d(ETIQUETA_LOG, "Respuesta ${response.code()} ${response.request().url().encodedPath()}")
        }
    }

    private fun registrarErrorConexion(request: Request, error: IOException) {
        if (BuildConfig.DEBUG) {
            Log.d(ETIQUETA_LOG, "Error de conexion en ${request.method()} ${request.url().encodedPath()}: ${error.javaClass.simpleName}")
        }
    }
}
