package com.undef.localhandsbrambillafunes.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.undef.localhandsbrambillafunes.data.dao.UserDao
import com.undef.localhandsbrambillafunes.data.entity.User
import com.undef.localhandsbrambillafunes.data.entity.UserRole
import com.undef.localhandsbrambillafunes.util.PasswordManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests para AuthRepository - Funcionalidad crítica de autenticación
 * 
 * Valida:
 * - Registro de nuevos usuarios
 * - Login con credenciales correctas/incorrectas
 * - Gestión de sesión de usuario
 * - Recuperación de contraseña
 * - Generación y verificación de códigos
 */
class AuthRepositoryTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var userDao: UserDao
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        // Mock dependencies
        userDao = mockk()
        context = mockk()
        sharedPreferences = mockk()
        sharedPreferencesEditor = mockk()

        // Setup SharedPreferences mock
        every { context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putInt(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putString(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.putBoolean(any(), any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.remove(any()) } returns sharedPreferencesEditor
        every { sharedPreferencesEditor.apply() } just Runs

        // Initialize repository
        authRepository = AuthRepository(userDao, context)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun registerUser_withNewEmail_success() = runTest {
        // Given
        val user = User(
            id = 0,
            name = "Test",
            lastName = "User",
            email = "test@example.com",
            password = "password123",
            phone = "1234567890",
            address = "Test Address",
            role = UserRole.CLIENT,
            photoUrl = null
        )
        val userId = 1L

        coEvery { userDao.isEmailExists(user.email) } returns 0
        coEvery { userDao.insertUser(any()) } returns userId

        // When
        val result = authRepository.registerUser(user)

        // Then
        assertTrue("Registro debe ser exitoso", result.isSuccess)
        assertEquals("Debe retornar el ID del usuario", userId, result.getOrNull())
        coVerify { userDao.isEmailExists(user.email) }
        coVerify { userDao.insertUser(match { it.email == user.email }) }
        verify { sharedPreferencesEditor.putInt("current_user_id", userId.toInt()) }
        verify { sharedPreferencesEditor.putBoolean("is_logged_in", true) }
    }

    @Test
    fun registerUser_withExistingEmail_fails() = runTest {
        // Given
        val user = User(
            id = 0,
            name = "Test",
            lastName = "User",
            email = "existing@example.com",
            password = "password123",
            phone = "1234567890",
            address = "Test Address",
            role = UserRole.CLIENT,
            photoUrl = null
        )

        coEvery { userDao.isEmailExists(user.email) } returns 1

        // When
        val result = authRepository.registerUser(user)

        // Then
        assertTrue("Debe fallar con email existente", result.isFailure)
        assertEquals(
            "Debe retornar mensaje de error correcto",
            "El email ya esta registrado!",
            result.exceptionOrNull()?.message
        )
        coVerify { userDao.isEmailExists(user.email) }
        coVerify(exactly = 0) { userDao.insertUser(any()) }
    }

    @Test
    fun loginUser_withCorrectCredentials_success() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val hashedPassword = PasswordManager.hashPassword(password)
        val user = User(
            id = 1,
            name = "Test",
            lastName = "User",
            email = email,
            password = hashedPassword,
            phone = "1234567890",
            address = "Test Address",
            role = UserRole.CLIENT,
            photoUrl = null,
            isEmailVerified = true
        )

        coEvery { userDao.getUserByEmail(email) } returns user

        // When
        val result = authRepository.loginUser(email, password)

        // Then
        assertTrue("Login debe ser exitoso", result.isSuccess)
        assertEquals("Debe retornar el usuario", user, result.getOrNull())
        coVerify { userDao.getUserByEmail(email) }
        verify { sharedPreferencesEditor.putInt("current_user_id", user.id) }
        verify { sharedPreferencesEditor.putBoolean("is_logged_in", true) }
    }

    @Test
    fun loginUser_withIncorrectPassword_fails() = runTest {
        // Given
        val email = "test@example.com"
        val correctPassword = "password123"
        val incorrectPassword = "wrongpassword"
        val hashedPassword = PasswordManager.hashPassword(correctPassword)
        val user = User(
            id = 1,
            name = "Test",
            lastName = "User",
            email = email,
            password = hashedPassword,
            phone = "1234567890",
            address = "Test Address",
            role = UserRole.CLIENT,
            photoUrl = null,
            isEmailVerified = true
        )

        coEvery { userDao.getUserByEmail(email) } returns user

        // When
        val result = authRepository.loginUser(email, incorrectPassword)

        // Then
        assertTrue("Debe fallar con contraseña incorrecta", result.isFailure)
        assertEquals(
            "Debe retornar mensaje de error",
            "Credenciales incorrectos!",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun loginUser_withNonExistentEmail_fails() = runTest {
        // Given
        val email = "nonexistent@example.com"
        val password = "password123"

        coEvery { userDao.getUserByEmail(email) } returns null

        // When
        val result = authRepository.loginUser(email, password)

        // Then
        assertTrue("Debe fallar con email inexistente", result.isFailure)
        assertEquals(
            "Debe retornar mensaje de error",
            "Credenciales incorrectos!",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun loginUser_withUnverifiedEmail_fails() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val hashedPassword = PasswordManager.hashPassword(password)
        val user = User(
            id = 1,
            name = "Test",
            lastName = "User",
            email = email,
            password = hashedPassword,
            phone = "1234567890",
            address = "Test Address",
            role = UserRole.CLIENT,
            photoUrl = null,
            isEmailVerified = false
        )

        coEvery { userDao.getUserByEmail(email) } returns user

        // When
        val result = authRepository.loginUser(email, password)

        // Then
        assertTrue("Debe fallar con email no verificado", result.isFailure)
        assertEquals(
            "Debe retornar mensaje de error",
            "Email no Verificado!",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun getCurrentUserId_withActiveSession_returnsUserId() = runTest {
        // Given
        val userId = 1
        val user = mockk<User>()
        
        every { sharedPreferences.getInt("current_user_id", -1) } returns userId
        every { sharedPreferences.getBoolean("is_logged_in", false) } returns true
        coEvery { userDao.getUserById(userId) } returns user

        // When
        val result = authRepository.getCurrentUserId()

        // Then
        assertEquals("Debe retornar el ID del usuario", userId, result)
    }

    @Test
    fun getCurrentUserId_withNoSession_returnsNull() = runTest {
        // Given
        every { sharedPreferences.getInt("current_user_id", -1) } returns -1
        every { sharedPreferences.getBoolean("is_logged_in", false) } returns false

        // When
        val result = authRepository.getCurrentUserId()

        // Then
        assertNull("Debe retornar null sin sesión activa", result)
    }

    @Test
    fun isUserLoggedIn_withActiveSession_returnsTrue() {
        // Given
        every { sharedPreferences.getBoolean("is_logged_in", false) } returns true

        // When
        val result = authRepository.isUserLoggedIn()

        // Then
        assertTrue("Debe retornar true con sesión activa", result)
    }

    @Test
    fun isUserLoggedIn_withNoSession_returnsFalse() {
        // Given
        every { sharedPreferences.getBoolean("is_logged_in", false) } returns false

        // When
        val result = authRepository.isUserLoggedIn()

        // Then
        assertFalse("Debe retornar false sin sesión activa", result)
    }

    @Test
    fun logout_clearsUserSession() {
        // When
        authRepository.logout()

        // Then
        verify { sharedPreferencesEditor.remove("current_user_id") }
        verify { sharedPreferencesEditor.remove("user_email") }
        verify { sharedPreferencesEditor.putBoolean("is_logged_in", false) }
        verify { sharedPreferencesEditor.apply() }
    }

    @Test
    fun generateVerificationCode_generatesValidCode() = runTest {
        // Given
        val email = "test@example.com"
        
        coEvery { userDao.updateVerificationCode(any(), any()) } just Runs

        // When
        val result = authRepository.generateVerificationCode(email)

        // Then
        assertTrue("Debe generar código exitosamente", result.isSuccess)
        val code = result.getOrNull()
        assertNotNull("Código no debe ser nulo", code)
        assertEquals("Código debe tener 4 dígitos", 4, code?.length)
        assertTrue("Código debe ser numérico", code?.all { it.isDigit() } == true)
        assertTrue("Código debe estar entre 1000 y 9999", code?.toInt() in 1000..9999)
        coVerify { userDao.updateVerificationCode(email.lowercase(), any()) }
    }

    @Test
    fun verifyCode_withCorrectCode_success() = runTest {
        // Given
        val email = "test@example.com"
        val code = "1234"
        val expectedCode = "1234"

        // When
        val result = authRepository.verifyCode(email, code, expectedCode)

        // Then
        assertTrue("Verificación debe ser exitosa con código correcto", result.isSuccess)
        assertTrue("Debe retornar true", result.getOrNull() == true)
    }

    @Test
    fun verifyCode_withIncorrectCode_fails() = runTest {
        // Given
        val email = "test@example.com"
        val code = "1234"
        val expectedCode = "5678"

        // When
        val result = authRepository.verifyCode(email, code, expectedCode)

        // Then
        assertTrue("Verificación debe fallar con código incorrecto", result.isFailure)
        assertEquals(
            "Debe retornar mensaje de error",
            "Codigo incorrecto!",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun updatePassword_withValidEmail_success() = runTest {
        // Given
        val email = "test@example.com"
        val newPassword = "newPassword123"

        coEvery { userDao.updatePassword(any(), any()) } returns 1

        // When
        val result = authRepository.updatePassword(email, newPassword)

        // Then
        assertTrue("Actualización debe ser exitosa", result.isSuccess)
        assertTrue("Debe retornar true", result.getOrNull() == true)
        coVerify { userDao.updatePassword(email.lowercase(), match { it.startsWith("\$2a\$") }) }
    }

    @Test
    fun updatePassword_withInvalidEmail_fails() = runTest {
        // Given
        val email = "nonexistent@example.com"
        val newPassword = "newPassword123"

        coEvery { userDao.updatePassword(any(), any()) } returns 0

        // When
        val result = authRepository.updatePassword(email, newPassword)

        // Then
        assertTrue("Actualización debe fallar con email inválido", result.isFailure)
        assertNotNull("Debe retornar mensaje de error", result.exceptionOrNull())
    }

    @Test
    fun isEmailExists_withExistingEmail_returnsTrue() = runTest {
        // Given
        val email = "existing@example.com"
        
        coEvery { userDao.isEmailExists(email) } returns 1

        // When
        val result = authRepository.isEmailExists(email)

        // Then
        assertTrue("Debe ejecutarse exitosamente", result.isSuccess)
        assertTrue("Debe retornar true para email existente", result.getOrNull() == true)
    }

    @Test
    fun isEmailExists_withNonExistingEmail_returnsFalse() = runTest {
        // Given
        val email = "nonexistent@example.com"
        
        coEvery { userDao.isEmailExists(email) } returns 0

        // When
        val result = authRepository.isEmailExists(email)

        // Then
        assertTrue("Debe ejecutarse exitosamente", result.isSuccess)
        assertFalse("Debe retornar false para email no existente", result.getOrNull() == true)
    }

    @Test
    fun verifyResetCode_withCorrectCode_success() = runTest {
        // Given
        val email = "test@example.com"
        val code = "1234"
        val user = mockk<User>()
        
        coEvery { userDao.verifyCode(email.lowercase(), code) } returns user

        // When
        val result = authRepository.verifyResetCode(email, code)

        // Then
        assertTrue("Verificación debe ser exitosa", result.isSuccess)
        assertTrue("Debe retornar true", result.getOrNull() == true)
    }

    @Test
    fun verifyResetCode_withIncorrectCode_fails() = runTest {
        // Given
        val email = "test@example.com"
        val code = "1234"
        
        coEvery { userDao.verifyCode(email.lowercase(), code) } returns null

        // When
        val result = authRepository.verifyResetCode(email, code)

        // Then
        assertTrue("Verificación debe fallar", result.isFailure)
        assertEquals(
            "Debe retornar mensaje de error",
            "Código incorrecto",
            result.exceptionOrNull()?.message
        )
    }
}
