package com.undef.localhandsbrambillafunes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.undef.localhandsbrambillafunes.data.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM ProductEntity")
    fun getAllProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProduct(product: Product): Long

    @Query("SELECT * FROM ProductEntity WHERE category = :category")
    suspend fun getProductsByCategory(category: String): List<Product>

    @Query("SELECT * FROM ProductEntity WHERE location = :location")
    suspend fun getProductsByCity(location: String): List<Product>

    @Query("SELECT * FROM ProductEntity WHERE ownerId = :userId")
    fun getProductsByOwner(userId: Int): Flow<List<Product>>

    @Query("SELECT * FROM ProductEntity WHERE id = :id LIMIT 1")
    fun getProductById(id: Int): Flow<Product?>

    /**
     * Busca productos en la base de datos local según un criterio de texto.
     * La búsqueda es reactiva y se aplica sobre nombre, descripción, productor, categoría y ubicación.
     */
    @Query("SELECT * FROM ProductEntity WHERE " +
           "name LIKE '%' || :query || '%' OR " +
           "description LIKE '%' || :query || '%' OR " +
           "producer LIKE '%' || :query || '%' OR " +
           "category LIKE '%' || :query || '%' OR " +
           "location LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<Product>>

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)
}
