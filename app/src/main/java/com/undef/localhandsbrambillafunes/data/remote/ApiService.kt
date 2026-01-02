package com.undef.localhandsbrambillafunes.data.remote

import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.entity.Seller
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Interfaz que define los endpoints de la API REST para operaciones relacionadas con productos.
 *
 * Esta interfaz se utiliza con Retrofit para facilitar la comunicación HTTP con el servidor que provee
 * la información de productos. Incluye funciones para obtener todos los productos, agregar un nuevo producto,
 * y obtener productos filtrados por propietario.
 *
 * Todas las funciones son `suspend` y deben ser llamadas desde un entorno de corrutinas.
 */
interface ApiService {

    /**
     * Obtiene la lista completa de productos disponibles.
     *
     * Esta función realiza una solicitud HTTP GET al endpoint `/products`
     * y devuelve una lista de productos en formato JSON.
     *
     * @return Una lista de objetos [Product] obtenidos del servidor.
     */
    @GET("products")
    suspend fun getProducts(): List<Product>

    /**
     * Agrega un nuevo producto al servidor.
     *
     * Esta función realiza una solicitud HTTP POST al endpoint `/products`,
     * enviando un objeto [Product] en el cuerpo de la petición.
     *
     * @param product El producto a agregar al servidor.
     * @return El producto recién creado con su ID generado por el servidor.
     */
    @POST("products")
    suspend fun addProduct(@Body product: Product): Product

    /**
     * Obtiene los productos publicados por un vendedor específico.
     *
     * Esta función realiza una solicitud HTTP GET al endpoint `/products`
     * con el parámetro de consulta `ownerId` para filtrar los productos por su dueño.
     *
     * @param ownerId El ID del usuario dueño de los productos (vendedor/emprendedor).
     * @return Una lista de productos asociados al [ownerId] especificado.
     */
    @GET("products")
    suspend fun getProductsByOwner(@Query("ownerId") ownerId: Int?): List<Product>

    /**
     * Crea un nuevo vendedor en el servidor.
     *
     * Esta función realiza una solicitud HTTP POST al endpoint `/sellers`, enviando
     * un objeto [Seller] en el cuerpo de la petición. El servidor asigna un ID único
     * y retorna el vendedor creado.
     *
     * @param seller El objeto [Seller] que se desea registrar.
     * @return El vendedor recién creado, con su ID asignado por el servidor.
     */
    @POST("sellers")
    suspend fun createSeller(@Body seller: Seller): Seller

    /**
     * Obtiene la lista completa de vendedores registrados en el servidor.
     *
     * Esta función realiza una solicitud HTTP GET al endpoint `/sellers`,
     * y devuelve una lista de todos los vendedores disponibles en formato JSON.
     *
     * @return Una lista de objetos [Seller] disponibles en el servidor.
     */
    @GET("sellers")
    suspend fun getSellers(): List<Seller>

    /**
     * Obtiene un vendedor por su dirección de correo electrónico.
     *
     * Esta función realiza una solicitud HTTP GET al endpoint `/sellers`,
     * con el parámetro de consulta `email` para filtrar los vendedores por su dirección de correo electrónico.
     *
     * @GET("sellers?email={email}"
     *
     * @param email La dirección de correo electrónico del vendedor.
     * @return Una lista de objetos [Seller] que coinciden con la dirección de correo electrónico especificada.
     */
    @GET("sellers")
    suspend fun getSellersByEmail(@Query("email") email: String): List<Seller>
}
