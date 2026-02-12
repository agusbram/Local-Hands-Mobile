package com.undef.localhandsbrambillafunes.data.repository

import com.undef.localhandsbrambillafunes.data.dao.FavoriteDao
import com.undef.localhandsbrambillafunes.data.entity.Favorite
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.exception.NotAuthenticatedException
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests para FavoriteRepository - Funcionalidad crítica de favoritos
 * 
 * Valida:
 * - Agregar productos a favoritos
 * - Eliminar productos de favoritos
 * - Obtener lista de favoritos del usuario
 * - Manejo de errores de autenticación
 */
class FavoriteRepositoryTest {

    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        favoriteDao = mockk()
        authRepository = mockk()
        favoriteRepository = FavoriteRepository(favoriteDao, authRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun addFavorite_success() = runTest {
        // Given
        val favorite = Favorite(userId = 1, productId = 100)
        
        coEvery { favoriteDao.addFavorite(favorite) } just Runs

        // When
        favoriteRepository.addFavorite(favorite)

        // Then
        coVerify { favoriteDao.addFavorite(favorite) }
    }

    @Test
    fun removeFavorite_withAuthenticatedUser_success() = runTest {
        // Given
        val userId = 1
        val productId = 100
        
        coEvery { authRepository.getCurrentUserId() } returns userId
        coEvery { favoriteDao.removeFavByUserAndProduct(userId, productId) } just Runs

        // When
        favoriteRepository.removeFavorite(productId)

        // Then
        coVerify { authRepository.getCurrentUserId() }
        coVerify { favoriteDao.removeFavByUserAndProduct(userId, productId) }
    }

    @Test
    fun removeFavorite_withoutAuthenticatedUser_throwsException() = runTest {
        // Given
        val productId = 100
        
        coEvery { authRepository.getCurrentUserId() } returns null

        // When & Then
        try {
            favoriteRepository.removeFavorite(productId)
            fail("Debe lanzar NotAuthenticatedException")
        } catch (e: NotAuthenticatedException) {
            assertEquals("User not logged in", e.message)
        }

        coVerify { authRepository.getCurrentUserId() }
        coVerify(exactly = 0) { favoriteDao.removeFavByUserAndProduct(any(), any()) }
    }

    @Test
    fun getFavoritesForUser_withAuthenticatedUser_returnsFlow() = runTest {
        // Given
        val userId = 1
        val products = listOf(
            mockk<Product>(),
            mockk<Product>()
        )
        val flowProducts = flowOf(products)
        
        coEvery { authRepository.getCurrentUserId() } returns userId
        coEvery { favoriteDao.getFavoritesForUser(userId) } returns flowProducts

        // When
        val result = favoriteRepository.getFavoritesForUser()

        // Then
        assertEquals("Debe retornar el Flow de productos", flowProducts, result)
        coVerify { authRepository.getCurrentUserId() }
        coVerify { favoriteDao.getFavoritesForUser(userId) }
    }

    @Test
    fun getFavoritesForUser_withoutAuthenticatedUser_throwsException() = runTest {
        // Given
        coEvery { authRepository.getCurrentUserId() } returns null

        // When & Then
        try {
            favoriteRepository.getFavoritesForUser()
            fail("Debe lanzar NotAuthenticatedException")
        } catch (e: NotAuthenticatedException) {
            assertEquals("User not logged in", e.message)
        }

        coVerify { authRepository.getCurrentUserId() }
        coVerify(exactly = 0) { favoriteDao.getFavoritesForUser(any()) }
    }

    @Test
    fun addFavoriteForCurrentUser_withAuthenticatedUser_success() = runTest {
        // Given
        val userId = 1
        val productId = 100
        
        coEvery { authRepository.getCurrentUserId() } returns userId
        coEvery { favoriteDao.addFavorite(any()) } just Runs

        // When
        favoriteRepository.addFavoriteForCurrentUser(productId)

        // Then
        coVerify { authRepository.getCurrentUserId() }
        coVerify { 
            favoriteDao.addFavorite(match { 
                it.userId == userId && it.productId == productId 
            }) 
        }
    }

    @Test
    fun addFavoriteForCurrentUser_withoutAuthenticatedUser_throwsException() = runTest {
        // Given
        val productId = 100
        
        coEvery { authRepository.getCurrentUserId() } returns null

        // When & Then
        try {
            favoriteRepository.addFavoriteForCurrentUser(productId)
            fail("Debe lanzar NotAuthenticatedException")
        } catch (e: NotAuthenticatedException) {
            assertEquals("User not logged in", e.message)
        }

        coVerify { authRepository.getCurrentUserId() }
        coVerify(exactly = 0) { favoriteDao.addFavorite(any()) }
    }

    @Test
    fun getEmailsOfUsersInterestedInSeller_returnsEmailList() = runTest {
        // Given
        val sellerId = 1
        val emails = listOf("user1@example.com", "user2@example.com", "user3@example.com")
        
        coEvery { favoriteDao.getEmailsOfUsersInterestedInSeller(sellerId) } returns emails

        // When
        val result = favoriteRepository.getEmailsOfUsersInterestedInSeller(sellerId)

        // Then
        assertEquals("Debe retornar la lista de emails", emails, result)
        assertEquals("Debe retornar 3 emails", 3, result.size)
        coVerify { favoriteDao.getEmailsOfUsersInterestedInSeller(sellerId) }
    }

    @Test
    fun getEmailsOfUsersInterestedInSeller_withNoInterested_returnsEmptyList() = runTest {
        // Given
        val sellerId = 1
        val emails = emptyList<String>()
        
        coEvery { favoriteDao.getEmailsOfUsersInterestedInSeller(sellerId) } returns emails

        // When
        val result = favoriteRepository.getEmailsOfUsersInterestedInSeller(sellerId)

        // Then
        assertTrue("Debe retornar lista vacía", result.isEmpty())
        coVerify { favoriteDao.getEmailsOfUsersInterestedInSeller(sellerId) }
    }
}
