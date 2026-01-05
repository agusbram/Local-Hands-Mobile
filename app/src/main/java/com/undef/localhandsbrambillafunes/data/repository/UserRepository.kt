package com.undef.localhandsbrambillafunes.data.repository

import android.util.Log
import com.undef.localhandsbrambillafunes.data.dao.UserDao
import com.undef.localhandsbrambillafunes.data.entity.Seller
import com.undef.localhandsbrambillafunes.data.entity.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import com.undef.localhandsbrambillafunes.data.remote.ApiService

/**
 * Repositorio que actúa como intermediario entre la capa de datos (DAO) y la lógica de negocio.
 *
 * Proporciona una interfaz abstracta para acceder a operaciones relacionadas con usuarios,
 * y delega las llamadas al [UserDao].
 *
 * @property userDao Objeto DAO que proporciona acceso a los métodos de la base de datos.
 */
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val authRepository: AuthRepository,
    private val apiService: ApiService
) {
    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param user Usuario actualizado.
     */
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario.
     * @return Instancia de [User], o `null` si no se encuentra.
     */
    suspend fun getUserById(): User {
        val currentUserId = authRepository.getCurrentUserId()!!

        return userDao.getUserById(currentUserId)
    }

    /**
     * Obtiene un usuario por su ID sin utilizar Flow.
     *
     * Útil para operaciones puntuales donde no se requiere observar cambios en tiempo real,
     * como validaciones o actualizaciones específicas.
     *
     * @param userId Identificador único del usuario.
     * @return Instancia de [User] si existe, o `null` en caso contrario.
     */
    suspend fun getUserByIdNonFlow(userId: Int): User? {
        return userDao.getUserByIdNonFlow(userId)
    }

    /**
     * Obtiene un usuario por su ID.
     * Es necesario el Flow para que la UI reacciones a los cambios en la BD en tiempo real
     * @param id ID del usuario.
     * @return Instancia Flow de [User], o `null` si no se encuentra.
     */
    fun getUserById(id: Int): Flow<User?> {
        return userDao.getUserByIdFlow(id)
    }
}
