package com.undef.localhandsbrambillafunes.data.repository

import com.undef.localhandsbrambillafunes.data.dao.UserDao
import com.undef.localhandsbrambillafunes.data.entity.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio que actúa como intermediario entre la capa de datos (DAO) y la lógica de negocio.
 *
 * Proporciona una interfaz abstracta para acceder a operaciones relacionadas con usuarios,
 * y delega las llamadas al [UserDao].
 *
 * @property userDao Objeto DAO que proporciona acceso a los métodos de la base de datos.
 */
@Singleton
class UserRepository @Inject constructor(private val userDao: UserDao) {

    /**
     * Inserta un nuevo usuario.
     *
     * @param user Instancia del usuario.
     * @return ID generado por la base de datos.
     */
    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param user Usuario actualizado.
     */
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email Correo electrónico.
     * @return Instancia de [User], o `null` si no existe.
     */
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
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
