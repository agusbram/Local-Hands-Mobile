package com.undef.localhandsbrambillafunes.data.repository

import android.util.Log
import com.undef.localhandsbrambillafunes.data.dao.SellerDao
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
    private val apiService: ApiService,
    private val sellerDao: SellerDao
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

    /**
     * Elimina un usuario y todos los datos asociados a su cuenta.
     *
     * Este método se encarga de realizar un borrado completo del usuario tanto a nivel
     * remoto como local, asegurando la consistencia de los datos.
     *
     * Flujo de la operación:
     * - Verifica si el usuario está asociado a un vendedor en la base de datos local.
     * - En caso afirmativo:
     *   - Intenta eliminar el vendedor en la API remota.
     *   - Si la eliminación falla por un motivo distinto a "no encontrado" (404),
     *     se lanza una excepción.
     *   - Elimina el vendedor de la base de datos local (Room).
     * - Elimina el usuario de la base de datos local (Room), independientemente de
     *   si era vendedor o no.
     *
     * Este método debe ejecutarse dentro de una corrutina, ya que realiza operaciones
     * de red y de acceso a base de datos.
     *
     * @param userId Identificador único del usuario a eliminar.
     * @throws Exception Si ocurre un error durante la comunicación con la API remota
     *                   o si la eliminación del vendedor falla por un motivo no esperado.
     */
    suspend fun deleteUserAndAssociatedData(userId: Int) {
        // Verificar si el usuario también es un vendedor
        val seller = sellerDao.getSellerByIdNonFlow(userId)

        if (seller != null) {
            // SI EL USUARIO ES UN VENDEDOR

            // Intentar eliminar el vendedor de la API
            try {
                val response = apiService.deleteSeller(seller.id)
                if (!response.isSuccessful && response.code() != 404) {
                    // Si la API falla por una razón que no sea "no encontrado", lanzamos un error.
                    throw Exception("API no pudo eliminar al vendedor (código: ${response.code()})")
                }
            } catch (e: Exception) {
                // Captura errores de red o la excepción de arriba y la relanza
                throw Exception("Fallo en la comunicación con la API: ${e.message}")
            }

            // Eliminar el vendedor de la base de datos local (Room)
            sellerDao.deleteSeller(seller)
        }

        // PARA TODOS LOS USUARIOS (SEAN VENDEDORES O NO)
        // Obtener el usuario de Room para poder eliminarlo
        val user = userDao.getUserByIdNonFlow(userId)
        if (user != null) {
            // d. Eliminar el usuario de la base de datos local (Room)
            userDao.deleteUser(user)
        }
    }
}
