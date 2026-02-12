package com.undef.localhandsbrambillafunes.data.repository

import android.util.Log
import at.favre.lib.crypto.bcrypt.BCrypt
import com.undef.localhandsbrambillafunes.data.dao.SellerDao
import com.undef.localhandsbrambillafunes.data.dao.UserDao
import com.undef.localhandsbrambillafunes.data.entity.Seller
import com.undef.localhandsbrambillafunes.data.entity.User
import com.undef.localhandsbrambillafunes.data.entity.UserRole
import com.undef.localhandsbrambillafunes.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio que actúa como intermediario entre la capa de datos (DAO, API) y la lógica de negocio del usuario.
 *
 * Proporciona una interfaz abstracta para acceder a operaciones relacionadas con usuarios,
 * y delega las llamadas a la capa de datos correspondiente, manejando la lógica de
 * autenticación, sesión y eliminación de cuentas.
 */
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val apiService: ApiService,
    private val sellerDao: SellerDao,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    /**
     * Actualiza los datos de un usuario existente en la base de datos local.
     *
     * @param user El objeto [User] con la información actualizada.
     */
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    /**
     * Obtiene un usuario por su ID como un flujo reactivo.
     * Ideal para observar cambios en la UI.
     *
     * @param id El ID del usuario a buscar.
     * @return Un [Flow] que emite el [User] o `null` si no se encuentra.
     */
    fun getUserById(id: Int): Flow<User?> {
        return userDao.getUserByIdFlow(id)
    }

    /**
     * Obtiene un usuario por su ID de forma no reactiva para operaciones puntuales.
     *
     * @param userId El ID del usuario a buscar.
     * @return El objeto [User] o `null` si no se encuentra.
     */
    suspend fun getUserByIdNonFlow(userId: Int): User? {
        return userDao.getUserByIdNonFlow(userId)
    }

    /**
     * Actualiza la contraseña del usuario actual en la base de datos.
     * La nueva contraseña se hashea con BCrypt antes de ser guardada.
     *
     * @param newPassword La nueva contraseña en texto plano.
     */
    suspend fun updateUserPassword(newPassword: String) {
        val userId = userPreferencesRepository.userIdFlow.firstOrNull() ?: return
        val user = getUserByIdNonFlow(userId)
        if (user != null) {
            val hashedPassword = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray())
            userDao.updatePassword(user.email, hashedPassword)
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     * Esto se logra limpiando los datos de sesión almacenados en [UserPreferencesRepository].
     */
    suspend fun logout() {
        // CORREGIDO: El nombre correcto de la función es clearUserSession.
        userPreferencesRepository.clearUserSession()
    }

    /**
     * Elimina la cuenta del usuario actual del sistema.
     * Orquesta la eliminación de todos los datos asociados al usuario y luego cierra la sesión.
     */
    suspend fun deleteUserAccount() {
        val userId = userPreferencesRepository.userIdFlow.firstOrNull() ?: return
        deleteUserAndAssociatedData(userId)
        logout() // Cierra la sesión después de eliminar la cuenta
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

    /**
     * Crea usuarios automáticamente a partir de los vendedores sincronizados.
     *
     * Este método toma los vendedores de la base de datos local y crea
     * usuarios correspondientes con role = SELLER si no existen.
     *
     * Útil para sincronizaciones iniciales donde solo hay vendedores en la API.
     */
    suspend fun createUsersFromSellers(sellers: List<Seller>) {
        try {
            Log.d("UserRepository", "🔄 Iniciando creación de usuarios a partir de vendedores...")

            var createdCount = 0
            
            for (seller in sellers) {
                try {
                    // Verificar si ya existe un usuario con este ID
                    val existingUser = userDao.getUserByIdNonFlow(seller.id)
                    
                    if (existingUser == null) {
                        // Crear un nuevo usuario a partir del vendedor
                        val newUser = User(
                            id = seller.id,
                            name = seller.name,
                            lastName = seller.lastname,
                            email = seller.email,
                            password = "", // Sin contraseña (viene del servidor)
                            phone = seller.phone,
                            address = seller.address,
                            role = UserRole.SELLER,
                            photoUrl = seller.photoUrl
                        )
                        
                        userDao.insertUser(newUser)
                        createdCount++
                        Log.d("UserRepository", "✅ Usuario creado a partir de vendedor: ${seller.name} (ID: ${seller.id})")
                    } else {
                        // Actualizar usuario existente con datos del vendedor
                        val updatedUser = existingUser.copy(
                            name = seller.name,
                            lastName = seller.lastname,
                            email = seller.email,
                            phone = seller.phone,
                            address = seller.address,
                            photoUrl = seller.photoUrl
                        )
                        userDao.updateUser(updatedUser)
                        Log.d("UserRepository", "♻️ Usuario actualizado a partir de vendedor: ${seller.name} (ID: ${seller.id})")
                    }
                } catch (e: Exception) {
                    Log.e("UserRepository", "❌ Error creando usuario para vendedor ${seller.id}: ${e.message}", e)
                }
            }

            Log.d("UserRepository", "✅ Creación de usuarios completada: $createdCount usuarios creados")
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Error creando usuarios desde vendedores: ${e.message}", e)
            e.printStackTrace()
        }
    }
}
