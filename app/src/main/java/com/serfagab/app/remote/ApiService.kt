package com.serfagab.app.remote

import com.serfagab.app.model.DetalleOrdenCompra
import com.serfagab.app.model.Material
import com.serfagab.app.model.OrdenCompra
import com.serfagab.app.model.Proveedor
import com.serfagab.app.model.TipoMaterial
import com.serfagab.app.model.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<Usuario>

    @GET("api/materiales")
    fun listarMateriales(): Call<List<Material>>

    @GET("api/tipos-material")
    fun listarTiposMaterial(): Call<List<TipoMaterial>>

    @GET("api/proveedores")
    fun listarProveedores(): Call<List<Proveedor>>

    @GET("api/ordenes-compra/usuario/{idUsuario}")
    fun listarOrdenesPorUsuario(@Path("idUsuario") idUsuario: Int): Call<List<OrdenCompra>>

    @POST("api/ordenes-compra/{idUsuario}/crear")
    fun crearOrdenCompra(
        @Path("idUsuario") idUsuario: Int,
        @Body orden: OrdenCompra
    ): Call<OrdenCompra>

    @POST("api/ordenes-compra/{idOrden}/agregar-detalle")
    fun agregarDetalleOrden(
        @Path("idOrden") idOrden: Int,
        @Body detalle: DetalleOrdenCompra
    ): Call<DetalleOrdenCompra>
}
