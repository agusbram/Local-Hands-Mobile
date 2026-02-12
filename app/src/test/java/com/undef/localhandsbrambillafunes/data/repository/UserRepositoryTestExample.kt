package com.undef.localhandsbrambillafunes.data.repository

import com.undef.localhandsbrambillafunes.data.dao.UserDao
import com.undef.localhandsbrambillafunes.data.entity.User
import com.undef.localhandsbrambillafunes.data.entity.UserRole
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * EJEMPLO: Tests para UserRepository
 * 
 * Este es un ejemplo de cómo escribir tests para un repositorio.
 * Sigue este patrón para agregar tests a otros componentes.
 * 
 * PATRÓN AAA (Arrange-Act-Assert):
 * 1. Given (Arrange): Configura el escenario de prueba
 * 2. When (Act): Ejecuta la acción que quieres probar
 * 3. Then (Assert): Verifica que el resultado sea el esperado
 */
class UserRepositoryTestExample {

    // Declarar las dependencias mock
    private lateinit var userRepository: UserRepository
    private lateinit var userDao: UserDao
    private lateinit var apiService: com.undef.localhandsbrambillafunes.data.remote.ApiService
    private lateinit var sellerDao: com.undef.localhandsbrambillafunes.data.dao.SellerDao
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    /**
     * Se ejecuta ANTES de cada test
     * Inicializa los mocks y el objeto a probar
     */
    @Before
    fun setup() {
        // Crear mocks de las dependencias
        userDao = mockk()
        apiService = mockk()
        sellerDao = mockk()
        userPreferencesRepository = mockk()
        
        // Inicializar el objeto a probar con los mocks
        userRepository = UserRepository(
            userDao,
            apiService,
            sellerDao,
            userPreferencesRepository
        )
    }

    /**
     * Se ejecuta DESPUÉS de cada test
     * Limpia los mocks
     */
    @After
    fun tearDown() {
        clearAllMocks()
    }

    /**
     * EJEMPLO 1: Test básico con Flow
     * 
     * Nomenclatura: metodo_condicion_resultadoEsperado
     */
    @Test
    fun getUserById_withValidId_returnsUser() = runTest {
        // Given (Arrange) - Preparar datos de prueba
        val userId = 1
        val expectedUser = User(
            id = userId,
            name = "Test",
            lastName = "User",
            email = "test@example.com",
            password = "hashedPassword",
            phone = "1234567890",
            address = "Test Address",
            role = UserRole.USER,
            photoUrl = null
        )
        val userFlow = flowOf(expectedUser)
        
        // Configurar el mock para retornar el Flow
        every { userDao.getUserByIdFlow(userId) } returns userFlow

        // When (Act) - Ejecutar la acción
        val result = userRepository.getUserById(userId).first()

        // Then (Assert) - Verificar el resultado
        assertNotNull("El usuario no debe ser nulo", result)
        assertEquals("Debe retornar el usuario correcto", expectedUser, result)
        assertEquals("El nombre debe coincidir", "Test", result?.name)
        assertEquals("El email debe coincidir", "test@example.com", result?.email)
        
        // Verificar que se llamó al método correcto
        verify { userDao.getUserByIdFlow(userId) }
    }

    /**
     * EJEMPLO 2: Test con suspend function
     */
    @Test
    fun getUserByIdNonFlow_withValidId_returnsUser() = runTest {
        // Given
        val userId = 1
        val expectedUser = User(
            id = userId,
            name = "Test",
            lastName = "User",
            email = "test@example.com",
            password = "hashedPassword",
            phone = "1234567890",
            address = "Test Address",
            role = UserRole.USER,
            photoUrl = null
        )
        
        // Usar coEvery para suspend functions
        coEvery { userDao.getUserByIdNonFlow(userId) } returns expectedUser

        // When
        val result = userRepository.getUserByIdNonFlow(userId)

        // Then
        assertNotNull("El usuario no debe ser nulo", result)
        assertEquals("Debe retornar el usuario correcto", expectedUser, result)
        
        // Usar coVerify para verificar llamadas a suspend functions
        coVerify { userDao.getUserByIdNonFlow(userId) }
    }

    /**
     * EJEMPLO 3: Test con caso de error
     */
    @Test
    fun getUserByIdNonFlow_withInvalidId_returnsNull() = runTest {
        // Given
        val userId = 999 // ID que no existe
        
        coEvery { userDao.getUserByIdNonFlow(userId) } returns null

        // When
        val result = userRepository.getUserByIdNonFlow(userId)

        // Then
        assertNull("Debe retornar null para ID inválido", result)
        coVerify { userDao.getUserByIdNonFlow(userId) }
    }

    /**
     * EJEMPLO 4: Test con verificación de múltiples llamadas
     */
    @Test
    fun updateUser_withValidUser_updatesSuccessfully() = runTest {
        // Given
        val user = User(
            id = 1,
            name = "Updated",
            lastName = "User",
            email = "updated@example.com",
            password = "hashedPassword",
            phone = "9876543210",
            address = "New Address",
            role = UserRole.USER,
            photoUrl = null
        )
        
        // Configurar mock para que no haga nada (just Runs)
        coEvery { userDao.updateUser(user) } just Runs

        // When
        userRepository.updateUser(user)

        // Then
        // Verificar que se llamó exactamente una vez
        coVerify(exactly = 1) { userDao.updateUser(user) }
        
        // Verificar que se llamó con el usuario correcto
        coVerify { 
            userDao.updateUser(match { 
                it.id == 1 && 
                it.name == "Updated" && 
                it.email == "updated@example.com" 
            }) 
        }
    }

    /**
     * EJEMPLO 5: Test con excepción
     */
    @Test
    fun getUserByIdNonFlow_withDatabaseError_throwsException() = runTest {
        // Given
        val userId = 1
        val expectedException = Exception("Database error")
        
        coEvery { userDao.getUserByIdNonFlow(userId) } throws expectedException

        // When & Then
        try {
            userRepository.getUserByIdNonFlow(userId)
            fail("Debería haber lanzado una excepción")
        } catch (e: Exception) {
            assertEquals("Debe lanzar la excepción correcta", expectedException.message, e.message)
        }
        
        coVerify { userDao.getUserByIdNonFlow(userId) }
    }

    /**
     * TIPS PARA ESCRIBIR BUENOS TESTS:
     * 
     * 1. Un test = Una funcionalidad
     *    - Cada test debe verificar UNA sola cosa
     * 
     * 2. Nombres descriptivos
     *    - metodo_condicion_resultadoEsperado
     *    - Ejemplo: getUserById_withValidId_returnsUser
     * 
     * 3. Usar Given-When-Then
     *    - Given: Preparar el escenario
     *    - When: Ejecutar la acción
     *    - Then: Verificar el resultado
     * 
     * 4. Mensajes descriptivos en asserts
     *    - assertEquals("El nombre debe coincidir", expected, actual)
     *    - Ayuda a entender qué falló
     * 
     * 5. Limpiar después de cada test
     *    - @After fun tearDown() { clearAllMocks() }
     * 
     * 6. Aislar dependencias
     *    - Usar mocks para todas las dependencias
     *    - No acceder a recursos reales (DB, API, etc.)
     * 
     * 7. Probar casos edge
     *    - Valores nulos, listas vacías, errores
     *    - No solo el "happy path"
     * 
     * 8. Tests independientes
     *    - Cada test debe poder ejecutarse solo
     *    - No depender del orden de ejecución
     */
}

/**
 * CHEAT SHEET DE MOCKK:
 * 
 * // Crear mocks
 * val mock = mockk<MyClass>()
 * 
 * // Configurar comportamiento - funciones normales
 * every { mock.method() } returns value
 * every { mock.method() } throws Exception()
 * every { mock.method() } just Runs  // Para Unit functions
 * 
 * // Configurar comportamiento - suspend functions
 * coEvery { mock.suspendMethod() } returns value
 * coEvery { mock.suspendMethod() } throws Exception()
 * coEvery { mock.suspendMethod() } just Runs
 * 
 * // Verificar llamadas - funciones normales
 * verify { mock.method() }
 * verify(exactly = 1) { mock.method() }
 * verify(atLeast = 1) { mock.method() }
 * verify { mock.method(match { it > 0 }) }
 * 
 * // Verificar llamadas - suspend functions
 * coVerify { mock.suspendMethod() }
 * coVerify(exactly = 1) { mock.suspendMethod() }
 * 
 * // Limpiar mocks
 * clearAllMocks()
 * 
 * // Capturar argumentos
 * val slot = slot<String>()
 * every { mock.method(capture(slot)) } returns value
 * assertEquals("expected", slot.captured)
 */

/**
 * CHEAT SHEET DE COROUTINES TEST:
 * 
 * // Test con coroutines
 * @Test
 * fun testName() = runTest {
 *     // tu código con suspend functions
 * }
 * 
 * // Test con delay
 * @Test
 * fun testWithDelay() = runTest {
 *     launch {
 *         delay(1000)
 *         // código
 *     }
 *     advanceTimeBy(1000) // Avanzar tiempo virtual
 * }
 * 
 * // Test con Flow
 * @Test
 * fun testFlow() = runTest {
 *     val flow = flowOf(1, 2, 3)
 *     val result = flow.first()
 *     assertEquals(1, result)
 * }
 */
