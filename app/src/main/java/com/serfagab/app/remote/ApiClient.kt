package com.serfagab.app.remote

import com.google.gson.JsonParseException
import com.serfagab.app.model.DetalleOrdenCompra
import com.serfagab.app.model.Material
import com.serfagab.app.model.OrdenCompra
import com.serfagab.app.model.Proveedor
import com.serfagab.app.model.TipoMaterial
import com.serfagab.app.model.Usuario
import java.io.IOException
import retrofit2.Call
import retrofit2.Response

data class Respuesta<T>(
    val exito: Boolean,
    val datos: T?,
    val codigo: Int,
    val mensaje: String
)

object ApiClient {

    const val CODIGO_ERROR_CONEXION = -1
    const val CODIGO_ERROR_RESPUESTA = -2

    private const val MENSAJE_EXITO = "Operación exitosa"
    private const val MENSAJE_CONEXION = "No se pudo conectar con el servidor"
    private const val MENSAJE_RESPUESTA = "La respuesta del servidor no es válida"

    private val apiService = RetrofitClient.apiService

    fun login(request: LoginRequest): Respuesta<Usuario> {
        return ejecutar(apiService.login(request))
    }

    fun login(login: String, clave: String): Respuesta<Usuario> {
        return login(LoginRequest(login, clave))
    }

    fun listarMateriales(): Respuesta<List<Material>> {
        return ejecutar(apiService.listarMateriales())
    }

    fun obtenerMateriales(): Respuesta<List<Material>> {
        return listarMateriales()
    }

    fun listarTiposMaterial(): Respuesta<List<TipoMaterial>> {
        return ejecutar(apiService.listarTiposMaterial())
    }

    fun obtenerTipos(): Respuesta<List<TipoMaterial>> {
        return listarTiposMaterial()
    }

    fun listarProveedores(): Respuesta<List<Proveedor>> {
        return ejecutar(apiService.listarProveedores())
    }

    fun obtenerProveedores(): Respuesta<List<Proveedor>> {
        return listarProveedores()
    }

    fun listarOrdenesPorUsuario(idUsuario: Int): Respuesta<List<OrdenCompra>> {
        return ejecutar(apiService.listarOrdenesPorUsuario(idUsuario))
    }

    fun misOrdenes(idUsuario: Int): Respuesta<List<OrdenCompra>> {
        return listarOrdenesPorUsuario(idUsuario)
    }

    fun crearOrdenCompra(idUsuario: Int, orden: OrdenCompra): Respuesta<OrdenCompra> {
        return ejecutar(apiService.crearOrdenCompra(idUsuario, orden))
    }

    fun crearOrden(idUsuario: Int, orden: OrdenCompra): Respuesta<OrdenCompra> {
        return crearOrdenCompra(idUsuario, orden)
    }

    fun agregarDetalleOrden(idOrden: Int, detalle: DetalleOrdenCompra): Respuesta<DetalleOrdenCompra> {
        return ejecutar(apiService.agregarDetalleOrden(idOrden, detalle))
    }

    fun agregarDetalle(idOrden: Int, detalle: DetalleOrdenCompra): Respuesta<DetalleOrdenCompra> {
        return agregarDetalleOrden(idOrden, detalle)
    }

    private fun <T> ejecutar(peticion: Call<T>): Respuesta<T> {
        return try {
            val respuesta: Response<T> = peticion.execute()
            if (respuesta.isSuccessful) {
                Respuesta(true, respuesta.body(), respuesta.code(), MENSAJE_EXITO)
            } else {
                Respuesta(false, null, respuesta.code(), obtenerMensajeError(respuesta.code()))
            }
        } catch (e: IOException) {
            Respuesta(false, null, CODIGO_ERROR_CONEXION, MENSAJE_CONEXION)
        } catch (e: JsonParseException) {
            Respuesta(false, null, CODIGO_ERROR_RESPUESTA, MENSAJE_RESPUESTA)
        } catch (e: Exception) {
            Respuesta(false, null, CODIGO_ERROR_RESPUESTA, MENSAJE_RESPUESTA)
        }
    }

    private fun obtenerMensajeError(codigo: Int): String {
        return when (codigo) {
            401 -> "Credenciales invalidas o sesion no autorizada"
            403 -> "No tiene permisos para realizar esta operacion"
            404 -> "El recurso solicitado no existe"
            else -> "No se pudo completar la operacion (codigo $codigo)"
        }
    }
}
