package com.undef.localhandsbrambillafunes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.undef.localhandsbrambillafunes.data.entity.Product
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para acceder y manipular los datos de productos en la base de datos local.
 * Proporciona métodos para realizar operaciones CRUD sobre la entidad [Product], incluyendo
 * filtrado por categoría, ciudad, nombre del vendedor, y funcionalidades para manejar favoritos.
 */
@Dao
interface ProductDao {

    /**
     * Consulta SQL para obtener todos los productos de la tabla de la base de datos
     * Obtiene todos los productos almacenados en la base de datos como un flujo reactivo.
     * @return [Flow] que emite una lista de todos los productos.
     */
    @Query("SELECT * FROM ProductEntity")
    fun getAllProducts(): Flow<List<Product>>

    /**
     * Obtiene una lista de todos los nombres de categorías únicos de la base de datos.
     *
     * @return Un [Flow] que emite una lista de [String] con los nombres de las categorías.
     */
    @Query("SELECT DISTINCT category FROM ProductEntity ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    /**
     * Obtiene una lista reactiva de productos que pertenecen a una categoría específica.
     *
     * @param category El nombre de la categoría para filtrar los productos.
     * @return Un [Flow] que emite la lista de productos de esa categoría.
     */
    @Query("SELECT * FROM ProductEntity WHERE category = :category")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    /**
     * Consulta SQL para insertar un producto en la tabla de la base de datos
     * @param product Producto a insertar.
     * @return ID generado del producto insertado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProduct(product: Product): Long

    /**
     * Recupera todos los productos asociados a un determinado usuario (vendedor).
     *
     * Esta consulta obtiene todos los registros de la tabla `ProductEntity` cuyo campo `ownerId`
     * coincida con el identificador del usuario proporcionado. El resultado se devuelve como
     * un `Flow`, lo que permite observar los cambios en tiempo real (por ejemplo, si se agregan,
     * actualizan o eliminan productos del vendedor).
     *
     * ## Parámetros:
     * @param userId ID del usuario (vendedor) del cual se desean recuperar los productos publicados.
     *
     * ## Retorno:
     * @return Un `Flow` que emite listas actualizadas de productos pertenecientes al usuario indicado.
     *
     * ## Uso típico:
     * Este método es útil para mostrar en pantalla los productos que un usuario ha creado, como
     * en una sección de “Mis productos” o “Administrar publicaciones”.
     *
     * ## Ejemplo de consulta SQL generada:
     * ```sql
     * SELECT * FROM ProductEntity WHERE ownerId = :userId
     * ```
     */
    @Query("SELECT * FROM ProductEntity WHERE ownerId = :userId")
    fun getProductsByOwner(userId: Int): Flow<List<Product>>

    /**
     * Realiza una búsqueda de productos en la base de datos según un texto dado.
     *
     * Este método ejecuta una consulta SQL que filtra los productos cuyos campos
     * `name`, `category`, `location` o `producer` contengan el texto indicado en
     * el parámetro `query`.
     *
     * La búsqueda no distingue entre coincidencias parciales, ya que utiliza
     * el operador `LIKE` con comodines (`%`), permitiendo encontrar resultados
     * que contengan el texto en cualquier posición del campo.
     *
     * El resultado se expone como un [Flow], lo que permite observar cambios
     * reactivos en los datos y actualizar automáticamente la interfaz de usuario
     * cuando la información almacenada se modifica.
     *
     * @param query Texto utilizado como criterio de búsqueda.
     * @return Un [Flow] que emite una lista de productos que coinciden con el
     * criterio de búsqueda.
     */
    @Query("""
    SELECT * FROM ProductEntity 
    WHERE name LIKE '%' || :query || '%' COLLATE NOCASE
    OR category LIKE '%' || :query || '%' COLLATE NOCASE
    OR location LIKE '%' || :query || '%' COLLATE NOCASE
    OR producer LIKE '%' || :query || '%' COLLATE NOCASE
    """)
    fun searchProducts(query: String): Flow<List<Product>>

    /**
     * Recupera un producto desde la base de datos en función de su identificador, como un flujo reactivo.
     *
     * Esta función retorna un [Flow] que emitirá el producto correspondiente al ID proporcionado, si existe.
     * Si no se encuentra ningún producto con el ID especificado, el flujo emitirá `null`.
     *
     * El uso de [Flow] permite observar cambios en la base de datos y reaccionar ante ellos de forma automática.
     *
     * @param id El identificador único del producto que se desea consultar.
     * @return Un [Flow] que emite una instancia de [Product] si se encuentra, o `null` en caso contrario.
     */
    @Query("SELECT * FROM ProductEntity WHERE id = :id LIMIT 1")
    fun getProductById(id: Int): Flow<Product?>

    /**
     * Consulta SQL para actualizar un producto en la tabla de la base de datos
     * Necesario para cambiar el estado de favorito, actualizar info, etc.
     * @param product Producto a actualizar.
     */
    @Update
    suspend fun updateProduct(product: Product)

    /**
     * Elimina un producto específico de la base de datos.
     * Para eliminar un producto (por ejemplo, limpiar favoritos antiguos o si el admin borra uno).
     * @param product Producto a eliminar.
     * */
    @Delete
    suspend fun deleteProduct(product: Product)

    /**
     * Inserta una lista de productos en la base de datos, reemplazando los existentes si hay conflictos.
     * Requerido cuando se sincronizan datos desde el servidor
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)
}
