package com.undef.localhandsbrambillafunes.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests para PasswordManager - Funcionalidad crítica de seguridad
 * 
 * Valida:
 * - Generación correcta de hashes BCrypt
 * - Verificación de contraseñas correctas
 * - Rechazo de contraseñas incorrectas
 * - Manejo de casos edge
 */
class PasswordManagerTest {

    @Test
    fun hashPassword_generatesValidBCryptHash() {
        // Given
        val password = "TestPassword123!"
        
        // When
        val hash = PasswordManager.hashPassword(password)
        
        // Then
        assertNotNull("Hash no debe ser nulo", hash)
        assertTrue("Hash debe comenzar con $2a$", hash.startsWith("\$2a\$"))
        assertEquals("Hash BCrypt debe tener 60 caracteres", 60, hash.length)
    }

    @Test
    fun hashPassword_generatesDifferentHashesForSamePassword() {
        // Given
        val password = "TestPassword123!"
        
        // When
        val hash1 = PasswordManager.hashPassword(password)
        val hash2 = PasswordManager.hashPassword(password)
        
        // Then
        assertNotEquals("Cada hash debe ser único debido al salt aleatorio", hash1, hash2)
    }

    @Test
    fun checkPassword_returnsTrueForCorrectPassword() {
        // Given
        val password = "CorrectPassword123!"
        val hash = PasswordManager.hashPassword(password)
        
        // When
        val result = PasswordManager.checkPassword(password, hash)
        
        // Then
        assertTrue("Debe verificar correctamente la contraseña correcta", result)
    }

    @Test
    fun checkPassword_returnsFalseForIncorrectPassword() {
        // Given
        val correctPassword = "CorrectPassword123!"
        val incorrectPassword = "WrongPassword456!"
        val hash = PasswordManager.hashPassword(correctPassword)
        
        // When
        val result = PasswordManager.checkPassword(incorrectPassword, hash)
        
        // Then
        assertFalse("Debe rechazar contraseña incorrecta", result)
    }

    @Test
    fun checkPassword_returnsFalseForEmptyPassword() {
        // Given
        val password = "TestPassword123!"
        val hash = PasswordManager.hashPassword(password)
        
        // When
        val result = PasswordManager.checkPassword("", hash)
        
        // Then
        assertFalse("Debe rechazar contraseña vacía", result)
    }

    @Test
    fun checkPassword_returnsFalseForInvalidHash() {
        // Given
        val password = "TestPassword123!"
        val invalidHash = "invalid_hash_format"
        
        // When
        val result = PasswordManager.checkPassword(password, invalidHash)
        
        // Then
        assertFalse("Debe manejar hash inválido sin lanzar excepción", result)
    }

    @Test
    fun checkPassword_isCaseSensitive() {
        // Given
        val password = "TestPassword123!"
        val hash = PasswordManager.hashPassword(password)
        
        // When
        val result = PasswordManager.checkPassword("testpassword123!", hash)
        
        // Then
        assertFalse("Debe ser sensible a mayúsculas/minúsculas", result)
    }

    @Test
    fun hashPassword_handlesSpecialCharacters() {
        // Given
        val password = "P@ssw0rd!#\$%&*()_+-=[]{}|;:',.<>?/~`"
        
        // When
        val hash = PasswordManager.hashPassword(password)
        val result = PasswordManager.checkPassword(password, hash)
        
        // Then
        assertTrue("Debe manejar correctamente caracteres especiales", result)
    }

    @Test
    fun hashPassword_handlesUnicodeCharacters() {
        // Given
        val password = "Contraseña123!こんにちは"
        
        // When
        val hash = PasswordManager.hashPassword(password)
        val result = PasswordManager.checkPassword(password, hash)
        
        // Then
        assertTrue("Debe manejar correctamente caracteres Unicode", result)
    }

    @Test
    fun hashPassword_handlesLongPassword() {
        // Given - BCrypt tiene un límite de 72 bytes
        val password = "A".repeat(100)
        
        // When
        val hash = PasswordManager.hashPassword(password)
        val result = PasswordManager.checkPassword(password, hash)
        
        // Then
        assertTrue("Debe manejar contraseñas largas correctamente", result)
    }

    @Test
    fun hashPassword_handlesMinimumPassword() {
        // Given
        val password = "a"
        
        // When
        val hash = PasswordManager.hashPassword(password)
        val result = PasswordManager.checkPassword(password, hash)
        
        // Then
        assertTrue("Debe manejar contraseñas de un solo carácter", result)
    }

    @Test
    fun checkPassword_returnsFalseForNullHash() {
        // Given
        val password = "TestPassword123!"
        val nullHash = ""
        
        // When
        val result = PasswordManager.checkPassword(password, nullHash)
        
        // Then
        assertFalse("Debe manejar hash vacío sin lanzar excepción", result)
    }
}
