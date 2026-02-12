package com.undef.localhandsbrambillafunes.util

import at.favre.lib.crypto.bcrypt.BCrypt

/**
 * Utilidad centralizada para la gestión segura de contraseñas.
 *
 * Este objeto encapsula la lógica de:
 * - Generación de hashes de contraseñas usando BCrypt.
 * - Verificación de contraseñas en texto plano contra hashes almacenados.
 *
 * Su uso garantiza consistencia criptográfica en toda la aplicación
 * (por ejemplo, entre autenticación y cambio de contraseña).
 *
 * @see at.favre.lib.crypto.bcrypt.BCrypt
 */
object PasswordManager {
    /**
     * Genera un hash BCrypt.
     * BCrypt tiene un límite de 72 bytes. Si la contraseña es más larga,
     * la recortamos manualmente para evitar el error 'IllegalArgumentException'.
     */
    fun hashPassword(password: String): String {
        // Recortamos a 72 caracteres para asegurar compatibilidad con el límite de BCrypt
        val safePassword = if (password.length > 72) password.substring(0, 72) else password

        return BCrypt.withDefaults().hashToString(12, safePassword.toCharArray())
    }

    /**
     * Verifica si la contraseña coincide con el hash.
     * También aplicamos el recorte aquí para que coincida con el hash generado arriba.
     */
    fun checkPassword(plainPassword: String, hashedPassword: String): Boolean {
        return try {
            val safePassword = if (plainPassword.length > 72) plainPassword.substring(0, 72) else plainPassword

            val result = BCrypt.verifyer().verify(safePassword.toCharArray(), hashedPassword)
            result.verified
        } catch (e: Exception) {
            // Retornamos false si el hash está mal formado o hay un error
            false
        }
    }
}