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
     * Genera un hash BCrypt a partir de una contraseña en texto plano.
     *
     * - Utiliza el algoritmo BCrypt con un coste de trabajo (cost factor) de 12.
     * - El resultado incluye el salt y el coste embebidos en el propio hash.
     * - La contraseña original nunca debe almacenarse ni persistirse.
     *
     * @param password Contraseña en texto plano proporcionada por el usuario.
     * @return Cadena que representa el hash BCrypt de la contraseña.
     *
     * @throws IllegalArgumentException si la contraseña es inválida.
     */
    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    /**
     * Verifica si una contraseña en texto plano coincide con un hash BCrypt almacenado.
     *
     * - Extrae automáticamente el salt y el coste desde el hash.
     * - Realiza la comparación de forma segura contra ataques de timing.
     * - Devuelve `false` si el hash no tiene un formato válido.
     *
     * @param plainPassword Contraseña ingresada por el usuario en texto plano.
     * @param hashedPassword Hash BCrypt previamente almacenado.
     * @return `true` si la contraseña coincide con el hash; `false` en caso contrario.
     */
    fun checkPassword(plainPassword: String, hashedPassword: String): Boolean {
        return try {
            val result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword)
            result.verified
        } catch (e: Exception) {
            // Esto puede pasar si el hash no tiene el formato esperado
            false
        }
    }
}