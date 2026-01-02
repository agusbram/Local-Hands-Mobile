package com.undef.localhandsbrambillafunes.data.repository

import com.undef.localhandsbrambillafunes.data.dao.UserDao
import com.undef.localhandsbrambillafunes.data.entity.Seller
import com.undef.localhandsbrambillafunes.data.entity.User
import com.undef.localhandsbrambillafunes.data.remote.ApiService
import javax.inject.Inject

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
    private val api: ApiService
) {

    /**
     * Registra un nuevo vendedor (Seller) en el servidor remoto.
     *
     * Esta función delega en el servicio de red (`ApiService`) la tarea de enviar una
     * solicitud POST al endpoint correspondiente con la información del nuevo vendedor.
     *
     * @param seller El objeto [Seller] que contiene los datos del vendedor a registrar.
     * @return El objeto [Seller] que representa al vendedor creado, incluyendo su ID asignado por el servidor.
     */
    suspend fun createSeller(seller: Seller): Seller = api.createSeller(seller)

    /**
     * Obtiene la lista de todos los vendedores registrados.
     *
     * Esta función realiza una llamada al servicio de red (`ApiService`) para obtener todos
     * los vendedores almacenados en el servidor remoto.
     *
     * @return Una lista de objetos [Seller] disponibles en el servidor.
     */
    suspend fun getSellers(): List<Seller> = api.getSellers()

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
     * Elimina un usuario.
     *
     * @param user Usuario a eliminar.
     */
//    suspend fun deleteUser(user: User) {
//        userDao.deleteUser(user)
//    }

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
     * Verifica si un usuario es un vendedor.
     *
     * @param email Correo electrónico del usuario.
     * @return `true` si el usuario es un vendedor, `false` en caso contrario.
     */
    suspend fun isUserSeller(email: String): Boolean {
        val sellers = api.getSellersByEmail(email) // Retrofit: @GET("sellers?email={email}")
        return sellers.isNotEmpty()
    }

    /**
     * Obtiene todos los usuarios.
     *
     * @return Lista de usuarios.
     */
//    suspend fun getAllUsers(): List<User> {
//        return userDao.getAllUsers()
//    }

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email Correo electrónico.
     * @return Instancia de [User], o `null` si no existe.
     */
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }
}
