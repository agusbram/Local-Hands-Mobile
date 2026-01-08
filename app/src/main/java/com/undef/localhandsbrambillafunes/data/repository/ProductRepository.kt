package com.undef.localhandsbrambillafunes.data.repository

import com.undef.localhandsbrambillafunes.data.dao.FavoriteDao
import com.undef.localhandsbrambillafunes.data.dao.ProductDao
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.entity.Favorite
import com.undef.localhandsbrambillafunes.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * El flujo recomendado es el siguiente: **DAO → Repository → ViewModel → UI (Compose)**.
 *
 * Utilizamos la arquitectura MVVM: Model-View-ViewModel.
 *
 * Encargado de conectar el DAO con el ViewModel.
 *
 * **Responsabilidad principal**: Abstraer el acceso a múltiples fuentes de datos (local, remoto,
 * cache, etc.) y proporcionar una API limpia al resto de la aplicación.
 *
 * **¿Para qué sirve?**
 *
 * --> Intermediario entre la base de datos (Room) y el ViewModel.
 *
 * --> Encapsula la lógica de acceso a datos y la forma en que se obtienen (por ejemplo, Room, Retrofit, DataStore, etc.).
 *
 * --> Facilita pruebas unitarias porque se puede simular fácilmente.
 *
 * --> Mejora la escalabilidad y mantenibilidad del código.
 *
 * @property productDao Acceso local a los productos (Room).
 * @property favoriteDao Acceso local a los productos favoritos (Room).
 * @property api Servicio que interactúa con la API remota de productos.
 * */
class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val favoriteDao: FavoriteDao,
    private val api: ApiService
    ) {
    /**
     * Obtiene todos los productos disponibles desde la API remota.
     *
     * Esta función realiza una solicitud HTTP GET al endpoint `/products`
     * a través de Retrofit, devolviendo una lista de productos en formato JSON.
     *
     * @return Lista de [Product] obtenidos del servidor.
     * @throws IOException si hay errores de red.
     */
    suspend fun getProducts() = api.getProducts()

    /**
     * Obtiene los productos publicados por un usuario emprendedor específico.
     *
     * Esta función consulta el endpoint `/products` agregando el parámetro
     * de consulta `ownerId` para filtrar por productos del dueño.
     *
     * @param ownerId ID del usuario que publicó los productos.
     * @return Lista de [Product] asociados al dueño especificado.
     * @throws IOException si la red falla o la API devuelve un error.
     */
    suspend fun getProductsByOwnerId(ownerId: Int?) = api.getProductsByOwner(ownerId)

    /**
     * Envía un nuevo producto al servidor para que sea persistido.
     *
     * Esta función realiza una solicitud HTTP POST al endpoint `/products`
     * enviando los datos del producto en formato JSON como cuerpo de la petición.
     *
     * @param product Instancia de [Product] que se desea agregar al backend.
     * @return El producto creado con su ID asignado por el servidor.
     * @throws IOException en caso de fallo en la conexión o error de la API.
     */
    suspend fun addProduct(product: Product) = api.addProduct(product)

    /**
     * Obtiene todos los productos como un flujo reactivo.
     *
     * @return Un [Flow] que emite listas de productos almacenados en la base de datos.
     */
    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }

    /**
     * Obtiene un producto específico por su identificador.
     *
     * Esta función consulta la base de datos para recuperar un producto que coincida con el ID proporcionado.
     *
     * @param id El identificador único del producto que se desea obtener.
     * @return Una instancia de [Product] correspondiente al ID proporcionado, o `null` si no se encuentra ningún producto con ese ID.
     */
    fun getProductById(id: Int) = productDao.getProductById(id)

    /**
     * Obtiene un flujo reactivo con todos los productos publicados por un usuario específico.
     *
     * Este método consulta la base de datos local y retorna los productos cuyo `ownerId`
     * coincida con el ID proporcionado. El resultado se entrega como un `Flow`, lo cual permite
     * que la UI se actualice automáticamente si los datos cambian.
     *
     * @param ownerId Identificador único del usuario (vendedor) cuyos productos se desean obtener.
     * @return Un flujo (`Flow`) que emite listas de productos asociados al vendedor.
     */
    fun getProductsByOwner(ownerId: Int): Flow<List<Product>> =
        productDao.getProductsByOwner(ownerId)

    /**
     * Obtiene una lista de productos filtrados por una categoría específica.
     *
     * @param category Categoría por la cual se desea filtrar los productos.
     * @return Lista de productos que pertenecen a la categoría proporcionada.
     */
    suspend fun getProductsByCategory(category: String): List<Product> =
        withContext(Dispatchers.IO) {
            productDao.getProductsByCategory(category)
        }

    /**
     * Inserta un nuevo producto en la base de datos.
     *
     * @param product Instancia del producto a insertar.
     * @return El ID generado para el nuevo producto.
     */
    suspend fun insertProduct(product: Product): Long = withContext(Dispatchers.IO) {
        productDao.addProduct(product)
    }

    /**
     * Obtiene productos según la ciudad indicada.
     *
     * @param location Ciudad donde se encuentran los productos.
     * @return Lista de productos localizados en la ciudad especificada.
     */
    suspend fun getProductsByCity(location: String): List<Product> = withContext(Dispatchers.IO) {
        productDao.getProductsByCity(location)
    }

    /**
     * Busca productos cuyo nombre de vendedor coincida parcial o totalmente con el nombre indicado.
     *
     * @param name Nombre o parte del nombre del vendedor.
     * @return Lista de productos asociados a vendedores con ese nombre.
     */
    suspend fun searchProductsBySeller(name: String): List<Product> = withContext(Dispatchers.IO) {
        productDao.searchProductsBySeller(name)
    }

    /**
     * Actualiza los datos de un producto existente en la base de datos.
     *
     * @param product Producto con la información actualizada.
     */
    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    /**
     * Elimina un producto de la base de datos.
     *
     * @param product Producto que se desea eliminar.
     */
    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    /**
     * Inserta una lista de productos en la base de datos, reemplazando los existentes si hay conflicto.
     * Ideal para sincronización masiva desde un servidor remoto.
     *
     * @param products Lista de productos a insertar o actualizar.
     */
    suspend fun insertAll(products: List<Product>) = withContext(Dispatchers.IO) {
        productDao.insertAll(products)
    }

    // --- FAVORITOS ---

    /**
     * Obtiene un flujo reactivo con todos los productos marcados como favoritos por un usuario.
     *
     * Este método realiza una consulta `JOIN` entre las tablas de productos y favoritos,
     * devolviendo los productos que han sido agregados como favoritos por el usuario identificado.
     *
     * @param userId ID del usuario del cual se desean obtener los productos favoritos.
     * @return Un `Flow` que emite listas de productos favoritos en tiempo real.
     */
    fun getFavoritesForUser(userId: Int): Flow<List<Product>> =
        favoriteDao.getFavoritesForUser(userId)

    /**
     * Agrega un producto a la lista de favoritos de un usuario.
     *
     * Esta operación inserta una nueva entrada en la tabla de favoritos (`FavoriteEntity`)
     * asociando el `userId` con el `productId` correspondiente.
     * Si ya existía una relación igual, será reemplazada.
     *
     * @param userId ID del usuario que marca el producto como favorito.
     * @param productId ID del producto que se desea agregar a la lista de favoritos.
     */
    suspend fun addFavorite(userId: Int, productId: Int) = withContext(Dispatchers.IO) {
        favoriteDao.addFavorite(Favorite(userId, productId))
    }

    /**
     * Elimina un producto de la lista de favoritos de un usuario.
     *
     * Esta operación elimina la entrada correspondiente en la tabla de favoritos (`FavoriteEntity`)
     * que vincula al usuario con el producto indicado.
     *
     * @param userId ID del usuario que desea eliminar el producto de sus favoritos.
     * @param productId ID del producto a eliminar de la lista de favoritos.
     */
    suspend fun removeFavorite(userId: Int, productId: Int) = withContext(Dispatchers.IO) {
        favoriteDao.removeFavByUserAndProduct(userId, productId)
    }
}