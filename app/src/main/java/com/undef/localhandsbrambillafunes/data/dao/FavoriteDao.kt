package com.undef.localhandsbrambillafunes.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.undef.localhandsbrambillafunes.data.entity.Favorite
import com.undef.localhandsbrambillafunes.data.entity.Product
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz DAO (Data Access Object) para gestionar la relación de favoritos entre usuarios y productos.
 *
 * Esta interfaz proporciona métodos para insertar, eliminar y consultar productos marcados como favoritos
 * por los usuarios. Está diseñada para ser utilizada con la librería Room de Android.
 *
 * ## Operaciones disponibles:
 * - Agregar un producto a la lista de favoritos de un usuario.
 * - Eliminar un producto de la lista de favoritos.
 * - Consultar todos los productos favoritos de un usuario específico.
 */
@Dao
interface FavoriteDao {

    /**
     * Inserta un nuevo registro de favorito en la base de datos.
     *
     * Si ya existe un favorito con la misma combinación de `Id` y `productId`,
     * se reemplazará gracias a la política `OnConflictStrategy.REPLACE`.
     *
     * @param favorite Objeto que representa la relación entre el usuario y el producto favorito.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: Favorite)

    /**
     * Elimina un producto favorito específico de un usuario en la base de datos.
     *
     * Esta función ejecuta una consulta SQL que elimina una entrada de la tabla `favoriteentity`
     * que coincida con el identificador del usuario y el identificador del producto proporcionados.
     *
     * Es una operación suspendida y debe llamarse dentro de una corrutina o de otra función `suspend`.
     *
     * @param Id El identificador del usuario asociado al producto favorito.
     * @param productId El identificador del producto que se desea eliminar de los favoritos.
     */
    @Query("DELETE FROM favoriteentity WHERE userId = :id AND productId = :productId")
    suspend fun removeFavByUserAndProduct(id: Int, productId: Int)

    /**
     * Recupera todos los productos que han sido marcados como favoritos por un usuario determinado.
     *
     * Esta consulta realiza un `INNER JOIN` entre la tabla de productos (`ProductEntity`)
     * y la tabla de favoritos (`FavoriteEntity`) para retornar únicamente los productos asociados
     * al `Id` especificado.
     *
     * @param Id ID del usuario del cual se desean obtener los productos favoritos.
     * @return Un `Flow` reactivo que emite la lista de productos favoritos cada vez que cambia.
     */
    @Query("""
        SELECT p.* FROM ProductEntity p 
        INNER JOIN FavoriteEntity f ON p.id = f.productId 
        WHERE f.userId = :userId
    """)
    fun getFavoritesForUser(userId: Int): Flow<List<Product>>
}