package com.undef.localhandsbrambillafunes.data.repository

import android.util.Log
import com.undef.localhandsbrambillafunes.data.dao.FavoriteDao
import com.undef.localhandsbrambillafunes.data.dao.ProductDao
import com.undef.localhandsbrambillafunes.data.dto.ProductCreateDTO
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.entity.Favorite
import com.undef.localhandsbrambillafunes.data.model.ProductWithLocation
import com.undef.localhandsbrambillafunes.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
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
    private val api: ApiService,
    private val sellerRepository: SellerRepository
    ) {
    //--- PRODUCTOS EN API ---

    /**
     * Sincroniza los productos almacenados localmente con los obtenidos desde la API remota.
     *
     * Este método consulta la API para obtener la lista completa de productos
     * y los inserta o actualiza en la base de datos local (Room).
     *
     * En caso de error de red o excepción inesperada, la operación falla de forma silenciosa
     * y se imprime el stack trace para depuración.
     */
    suspend fun syncProductsWithApi() {
        try {
            Log.d("ProductRepository", "🔄 Iniciando sincronización de productos desde API...")
            val productsFromApi = api.getProducts()
            Log.d("ProductRepository", "📡 Se obtuvieron ${productsFromApi.size} productos de la API")

            var insertedCount = 0
            var updatedCount = 0
            
            productsFromApi.forEach { apiProduct ->
                try {
                    // Intentar insertar; si ya existe, se actualiza
                    productDao.addProduct(apiProduct)
                    insertedCount++
                    Log.d("ProductRepository", "✅ Producto procesado: ${apiProduct.name} (ID: ${apiProduct.id})")
                } catch (e: Exception) {
                    // Si falla por duplicado, intentar actualizar
                    try {
                        productDao.updateProduct(apiProduct)
                        updatedCount++
                        Log.d("ProductRepository", "♻️ Producto actualizado: ${apiProduct.name} (ID: ${apiProduct.id})")
                    } catch (updateError: Exception) {
                        Log.e("ProductRepository", "❌ Error procesando producto ${apiProduct.id}: ${updateError.message}", updateError)
                    }
                }
            }
            
            Log.d("ProductRepository", "✅ Sincronización de productos completada: $insertedCount insertados, $updatedCount actualizados")
        } catch (e: Exception) {
            Log.e("ProductRepository", "❌ Error sincronizando productos: ${e.message}", e)
            e.printStackTrace()
        }
    }

    /**
     * Agrega un producto intentando sincronizarlo con la API y la base de datos local.
     *
     * Flujo principal:
     * 1. Obtiene el nombre del emprendimiento del vendedor.
     * 2. Genera manualmente el próximo ID disponible.
     * 3. Construye un [ProductCreateDTO] con los datos del producto.
     * 4. Envía el producto a la API (POST).
     * 5. Inserta el producto creado en la base de datos local (Room).
     *
     * En caso de fallo al comunicarse con la API:
     * - Se genera un ID temporal basado en timestamp.
     * - El producto se guarda únicamente de forma local.
     *
     * @param product Producto a crear.
     * @return El producto creado, ya sea sincronizado con la API o almacenado localmente.
     */
    suspend fun addProductWithSync(product: Product): Product {
        return try {
            // Obtener el emprendimiento del vendedor
            val entrepreneurship = getEntrepreneurshipForOwner(product.ownerId)

            // Generar próximo ID manualmente
            val nextId = generateNextId()

            val productDto = ProductCreateDTO(
                id = nextId,  // Enviamos nuestro ID calculado
                name = product.name,
                description = product.description,
                producer = entrepreneurship ?: product.producer,
                category = product.category,
                ownerId = product.ownerId,
                images = product.images,
                price = product.price,
                location = product.location
            )

            // POST a API con ID incluido
            val createdProduct = api.addProductDTO(productDto)
            // Insertar en Room
            productDao.addProduct(createdProduct)
            createdProduct

        } catch (e: Exception) {
            // Fallback local
            val tempId = (System.currentTimeMillis() % 1000000).toInt()
            val localProduct = product.copy(id = tempId)
            productDao.addProduct(localProduct)
            localProduct
        }
    }

    /**
     * Obtiene el nombre del emprendimiento asociado a un propietario (vendedor).
     *
     * @param ownerId ID del usuario propietario del producto.
     * @return Nombre del emprendimiento o `null` si no existe o no se encuentra.
     */
    private suspend fun getEntrepreneurshipForOwner(ownerId: Int?): String? {
        return if (ownerId != null) {
             sellerRepository.getSellerByIdNonFlow(ownerId)?.entrepreneurship
        } else {
            null
        }
    }

    /**
     * Actualiza un producto tanto en la API como en la base de datos local.
     *
     * Flujo:
     * 1. Intenta actualizar el producto en la API.
     * 2. Actualiza el producto en Room.
     *
     * Si la API falla, el producto se actualiza únicamente de forma local.
     *
     * @param product Producto con los datos actualizados.
     * @return `true` si la sincronización con la API fue exitosa,
     *         `false` si solo se actualizó localmente.
     */
    suspend fun updateProductWithSync(product: Product): Boolean {
        return try {
            // Actualizar en API
            api.updateProduct(product.id, product)
            // Actualizar en Room
            productDao.updateProduct(product)
            true
        } catch (e: Exception) {
            // Si falla la API, actualizamos solo localmente
            productDao.updateProduct(product)
            // Indica que no se pudo sincronizar con API
            false
        }
    }

    /**
     * Actualiza el campo `producer` de todos los productos asociados a un propietario.
     *
     * Este método:
     * - Obtiene los productos del owner desde la base de datos local.
     * - Actualiza cada producto con el nuevo nombre de emprendimiento.
     * - Intenta sincronizar cada actualización con la API.
     * - Garantiza que Room se actualice incluso si la API falla.
     *
     * @param ownerId ID del propietario de los productos.
     * @param newProducer Nuevo nombre del emprendimiento/productor.
     */
    suspend fun updateProductsProducerByOwner(ownerId: Int, newProducer: String) {
        try {
            Log.d("ProductRepository", "Buscando productos con ownerId=$ownerId")

            // Obtener productos del owner desde la base de datos local
            val products = productDao.getProductsByOwner(ownerId).firstOrNull() ?: emptyList()

            Log.d("ProductRepository", "Encontrados ${products.size} productos para actualizar")

            if (products.isEmpty()) {
                Log.w("ProductRepository", "No se encontraron productos con ownerId=$ownerId")
                return
            }

            // Actualizar cada producto
            products.forEach { product ->
                Log.d("ProductRepository", "Actualizando producto ID=${product.id}, producer actual='${product.producer}' -> nuevo='$newProducer'")

                val updatedProduct = product.copy(producer = newProducer)

                // Actualizar en API
                try {
                    api.updateProduct(product.id, updatedProduct)
                    Log.d("ProductRepository", "Producto ${product.id} actualizado en API")
                } catch (e: Exception) {
                    Log.e("ProductRepository", "Error actualizando producto ${product.id} en API:  ${e.message}", e)
                }

                // Actualizar localmente (SIEMPRE, aunque falle la API)
                productDao.updateProduct(updatedProduct)
                Log.d("ProductRepository", "Producto ${product.id} actualizado en Room")
            }

            Log.d("ProductRepository", "Actualización completa:  ${products.size} productos procesados")

        } catch (e: Exception) {
            Log.e("ProductRepository", "Error crítico actualizando productos por owner", e)
            throw e
        }
    }

    /**
     * Elimina un producto intentando sincronizar la operación con la API y Room.
     *
     * Flujo:
     * 1. Intenta eliminar el producto en la API.
     * 2. Elimina el producto en la base de datos local.
     *
     * Si la API falla, el producto se elimina únicamente de forma local.
     *
     * @param product Producto a eliminar.
     * @return `true` si la eliminación fue sincronizada con la API,
     *         `false` si solo se eliminó localmente.
     */
    suspend fun deleteProductWithSync(product: Product): Boolean {
        return try {
            // Eliminar de API
            api.deleteProduct(product.id)
            // Eliminar de Room
            productDao.deleteProduct(product)
            true
        } catch (e: Exception) {
            // Si falla la API, eliminamos solo localmente
            productDao.deleteProduct(product)
            // Indica que no se pudo sincronizar con API
            false
        }
    }

    /**
     * Genera el próximo ID disponible para un producto.
     *
     * Estrategia:
     * - Consulta la API para obtener todos los productos.
     * - Busca el ID numérico más alto y retorna el siguiente.
     *
     * En caso de error de red:
     * - Genera un ID basado en el timestamp actual.
     *
     * @return ID único sugerido para un nuevo producto.
     */
    private suspend fun generateNextId(): Int {
        return try {
            val allProducts = api.getProducts()
            if (allProducts.isEmpty()) {
                1  // Primer producto
            } else {
                // Encontrar el máximo ID numérico
                val maxId = allProducts.maxOfOrNull { it.id } ?: 0
                maxId + 1
            }
        } catch (e: Exception) {
            // Si falla la consulta a la API, generar ID basado en timestamp
            (System.currentTimeMillis() % 1000000).toInt()
        }
    }

    // --- PRODUCTOS EN LOCAL ---

    /**
     * Obtiene todos los productos junto con la ubicación de su vendedor.
     */
    fun getAllProductsWithLocation(): Flow<List<ProductWithLocation>> {
        return productDao.getAllProductsWithLocation()
    }

    /**
     * Obtiene todos los productos como un flujo reactivo.
     *
     * @return Un [Flow] que emite listas de productos almacenados en la base de datos.
     */
    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }

    /**
     * Obtiene una lista de todos los nombres de categorías únicos como un flujo reactivo.
     */
    fun getAllCategories(): Flow<List<String>> = productDao.getAllCategories()

    /**
     * Obtiene una lista reactiva de productos que pertenecen a una categoría específica.
     */
    fun getProductsByCategory(category: String): Flow<List<Product>> = productDao.getProductsByCategory(category)

    /**
     * Busca productos en la base de datos local según un criterio de texto.
     *
     * Esta función realiza una búsqueda reactiva sobre los productos almacenados,
     * filtrando aquellos cuyos campos relevantes coincidan con el valor del
     * parámetro `query`. El resultado se expone como un [Flow], permitiendo que
     * la interfaz de usuario se actualice automáticamente ante cambios en los datos.
     *
     * La búsqueda tiene diversos filtros, y se aplica sobre atributos como el nombre,
     * la categoría, el vendedor y la ciudad.
     *
     * @param query Texto utilizado como criterio de búsqueda.
     * @return Un [Flow] que emite una lista de productos que coinciden con el criterio.
     */
    fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query)
    }

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
     * Obtiene un producto por su identificador.
     *
     * Esta función consulta la fuente de datos y retorna un [Flow] que emite
     * el [Product] correspondiente al `productId` proporcionado.
     * Si no existe un producto con ese identificador, el flujo emitirá `null`.
     *
     * El flujo se mantiene activo y emitirá nuevos valores si el producto
     * cambia en la base de datos.
     *
     * @param productId Identificador único del producto a buscar.
     * @return Un [Flow] que emite el [Product] encontrado o `null` si no existe.
     */
    fun getProductById(productId: Int): Flow<Product?> = productDao.getProductById(productId)

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