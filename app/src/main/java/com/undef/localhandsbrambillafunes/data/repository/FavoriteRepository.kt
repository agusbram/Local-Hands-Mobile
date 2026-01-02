package com.undef.localhandsbrambillafunes.data.repository

import com.undef.localhandsbrambillafunes.data.dao.FavoriteDao
import com.undef.localhandsbrambillafunes.data.entity.Favorite
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.exception.NotAuthenticatedException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repositorio encargado de manejar las operaciones relacionadas con la entidad `Favorite`.
 *
 * Esta clase actúa como intermediario entre el DAO (`FavoriteDao`) y las capas superiores de la aplicación
 * (como ViewModel o UseCase), encapsulando el acceso a los datos y permitiendo una mejor separación de responsabilidades.
 *
 * @param favoriteDao Instancia de `FavoriteDao` utilizada para ejecutar las operaciones sobre la base de datos.
 */
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val authRepository: AuthRepository // Añadir dependencia
) {

    /**
     * Agrega un nuevo favorito a la base de datos.
     *
     * @param favorite Objeto `Favorite` que se desea insertar.
     */
    suspend fun addFavorite(favorite: Favorite) {
        favoriteDao.addFavorite(favorite)
    }

    /**
     * Elimina un favorito existente de la base de datos.
     *
     * @param userId Identificador único del usuario.
     * @param productId Identificador único del producto.
     */
    suspend fun removeFavorite(productId: Int) {
        val currentUserId = authRepository.getCurrentUserId()
            ?: throw NotAuthenticatedException("User not logged in")

        favoriteDao.removeFavByUserAndProduct(currentUserId, productId)
    }

    /**
     * Obtiene la lista de elementos marcados como favoritos por un usuario específico.
     *
     * @param userId Identificador único del usuario.
     * @return Un `Flow` que emite la lista de objetos `Favorite` del usuario.
     */
    suspend fun getFavoritesForUser(): Flow<List<Product>> {
        val currentUserId = authRepository.getCurrentUserId()
            ?: throw NotAuthenticatedException("User not logged in")

        return favoriteDao.getFavoritesForUser(currentUserId)
    }

    /**
     * Agrega un producto a favoritos para el usuario actualmente autenticado.
     * @throws NotAuthenticatedException si no hay usuario autenticado
     */
    suspend fun addFavoriteForCurrentUser(productId: Int) {
        val currentUserId = authRepository.getCurrentUserId()
            ?: throw NotAuthenticatedException("User not logged in")

        val favorite = Favorite(
            userId = currentUserId,
            productId = productId
        )
        favoriteDao.addFavorite(favorite)
    }


}