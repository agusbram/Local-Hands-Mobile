package com.undef.localhandsbrambillafunes.data.dao

import androidx.room.*
import com.undef.localhandsbrambillafunes.data.entity.User
import kotlinx.coroutines.flow.Flow


/**
 * DAO (Data Access Object) para operaciones CRUD de usuarios
 *
 * @method getUserByEmail Obtiene usuario por email
 * @method authenticateUser Valida credenciales
 * @method insertUser Crea nuevo usuario
 * @method updatePassword Actualiza contraseña
 * @method isEmailExists Verifica si email está registrado
 * @method deleteAllUsers (DEBUG) Elimina todos los usuarios
 */
@Dao
interface UserDao {

    /**
     * Obtiene usuario por email
     *
     * @param email Email del usuario
     * @return Usuario o null si no existe
     */
    @Query("SELECT * FROM UserEntity WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    /**
     * Inserta nuevo usuario
     *
     * @param user Usuario a insertar
     * @return ID del usuario insertado
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    /**
     * Actualiza datos de usuario existente
     *
     * @param user Usuario con datos actualizados
     */
    @Update
    suspend fun updateUser(user: User)

    /**
     * Marca email como verificado y elimina código
     *
     * @param email Email a verificar
     */
    @Query("UPDATE UserEntity SET isEmailVerified = 1, verificationCode = NULL WHERE email = :email")
    suspend fun verifyEmail(email: String)

    /**
     * Actualiza código de verificación
     *
     * @param email Email del usuario
     * @param code Nuevo código de verificación
     */
    @Query("UPDATE UserEntity SET verificationCode = :code WHERE email = :email")
    suspend fun updateVerificationCode(email: String, code: String)

    /**
     * Verifica código de verificación
     *
     * @param email Email del usuario
     * @param code Código a verificar
     * @return Usuario si código válido, null si no
     */
    @Query("SELECT * FROM UserEntity WHERE email = :email AND verificationCode = :code COLLATE NOCASE LIMIT 1")
    suspend fun verifyCode(email: String, code: String): User?

    /**
     * Actualiza contraseña de usuario
     *
     * @param email Email del usuario
     * @param newPassword Nueva contraseña (debe estar hasheada)
     * @return Número de filas actualizadas
     */
    @Query("UPDATE UserEntity SET password = :newPassword WHERE email = :email COLLATE NOCASE")
    suspend fun updatePassword(email: String, newPassword: String): Int

    /**
     * Verifica existencia de email
     *
     * @param email Email a verificar
     * @return 1 si existe, 0 si no
     */
    @Query("SELECT COUNT(*) FROM UserEntity WHERE email = :email")
    suspend fun isEmailExists(email: String): Int

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario.
     * @return Instancia de [User], o `null` si no se encuentra.
     */
    @Query("SELECT * FROM UserEntity WHERE id = :id")
    suspend fun getUserById(id: Int): User

    /**
     * Obtiene un usuario por su ID.
     * Es necesario el Flow para que la UI reacciones a los cambios en la BD en tiempo real
     *
     * @param id ID del usuario.
     * @return Instancia Flow de [User], o `null` si no se encuentra.
     */
    @Query("SELECT * FROM UserEntity WHERE id = :id")
    fun getUserByIdFlow(id: Int): Flow<User?>

    /**
     * Obtiene un usuario por su identificador mediante una consulta única
     * (no reactiva).
     *
     * Es especialmente útil para:
     * - Validaciones puntuales (por ejemplo, verificación de contraseña).
     * - Operaciones internas de lógica de negocio.
     * - Casos donde no se requiere reactividad en la UI.
     *
     * @param userId Identificador único del usuario a buscar.
     * @return Instancia de [User] si existe un registro con el ID indicado,
     * o `null` si no se encuentra ningún usuario.
     */
    @Query("SELECT * FROM UserEntity WHERE id = :userId")
    suspend fun getUserByIdNonFlow(userId: Int): User?

    /**
     * Elimina todos los usuarios (SOLO PARA PRUEBAS)
     */
    @Query("DELETE FROM UserEntity")
    suspend fun deleteAllUsers()
}